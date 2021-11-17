/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URL;
import java.util.*;

public class BlackDuckReader implements BomReader {
    private static final Map<String, Relation.Type> USAGE_MAPPING = Map.of(
            "SEPARATE_WORK", Relation.Type.CONTAINS,
            "MERELY_AGGREGATED", Relation.Type.CONTAINS,
            "DEV_TOOL_EXCLUDED", Relation.Type.DEVELOPED_USING,
            "IMPLEMENTATION_OF_STANDARD", Relation.Type.DEPENDS_ON,
            "SOURCE_CODE", Relation.Type.DESCENDANT_OF,
            "STATICALLY_LINKED", Relation.Type.STATICALLY_LINKS,
            "DYNAMICALLY_LINKED", Relation.Type.DYNAMICALLY_LINKS);

    private final BlackDuckClient client;
    private final String token;
    private final String projectName;
    private final String versionName;
    private final Map<PackageURL, Package> packages = new HashMap<>();
    @SuppressWarnings("NotNullFieldNotInitialized")
    private BlackDuckProduct project;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private BlackDuckProduct projectVersion;

    public BlackDuckReader(URL url, String token, String projectName, String versionName, boolean skipSSL) {
        this(new BlackDuckClient(url, skipSSL), token, projectName, versionName);
    }

    BlackDuckReader(BlackDuckClient client, String token, String projectName, String versionName) {
        this.client = client;
        this.token = token;
        this.projectName = projectName;
        this.versionName = versionName;
    }

    @Override
    public void read(BillOfMaterials bom) {
        client.authenticate(token);

        final var serverVersion = client.getServerVersion();
        bom.setComment(String.format("Extracted from Black Duck server version %s", serverVersion));

        findProjectVersion(projectName, versionName);

        exportProjectVersion(bom);
    }

    private void findProjectVersion(String projectName, String versionName) {
        project = client.findProject(projectName)
                .orElseThrow(() -> new BlackDuckException("Found no project named '" + projectName + "'"));
        projectVersion = client.findProjectVersion(project.getId(), versionName)
                .orElseThrow(() -> new BlackDuckException("Found no version named '" + versionName + "' in project '" + project.getName()));
    }

    private void exportProjectVersion(BillOfMaterials bom) {
        System.out.println("Exporting Black Duck project '" + project.getName() + "', version '" + projectVersion.getName() + "'");
        System.out.println("Project: " + project.getId());
        System.out.println("Version: " + projectVersion.getId());
        System.out.println();

        exportProjectMetadata(bom);
        exportProjectComponents(bom);

        System.out.println("done");
    }

    private void exportProjectMetadata(BillOfMaterials bom) {
        bom.setTitle(project.getName() + " " + projectVersion.getName());
    }

    private void exportProjectComponents(BillOfMaterials bom) {
        System.out.print("Building tree of components ");
        final var root = new Package("", project.getName(), projectVersion.getName());
        project.getDescription().ifPresent(root::setDescription);
        projectVersion.getDescription().ifPresent(root::setSummary);
        projectVersion.getLicense().ifPresent(root::setConcludedLicense);
        projectVersion.getCreatedAt().ifPresent(bom::setCreatedAt);
        bom.addPackage(root);

        final var components = client.getRootComponents(project.getId(), projectVersion.getId());
        addChildren(bom, root, components, project.getId(), projectVersion.getId());
    }

    void addChildren(BillOfMaterials bom, @NullOr Package parent, List<BlackDuckComponent> components, UUID projectId, UUID versionId) {
        components.forEach(component -> {
            if (component.isSubproject()) {
                addSubproject(bom, parent, component.getId(), component.getVersionId(), component);
            } else {
                addChild(bom, parent, projectId, versionId, component);
            }
            System.out.print(".");
        });
    }

    private void addSubproject(BillOfMaterials bom, @NullOr Package parent, UUID projectId, UUID versionId, BlackDuckComponent component) {
        final Package pkg = exportAnonymousPackage(bom, parent, component);
        component.getLicense().ifPresent(pkg::setConcludedLicense);

        final var components = client.getRootComponents(projectId, versionId);
        addChildren(bom, pkg, components, projectId, versionId);
    }

    private Package exportAnonymousPackage(BillOfMaterials bom, @NullOr Package parent, BlackDuckComponent component) {
        final var pkg = new Package(null, component.getName(), component.getVersion());
        component.getLicense().ifPresent(pkg::setConcludedLicense);
        bom.addPackage(pkg);
        exportRelation(bom, parent, pkg, relationshipFor(component));
        return pkg;
    }

    private void addChild(BillOfMaterials bom, @NullOr Package parent, UUID projectId, UUID versionId, BlackDuckComponent component) {
        final var purls = component.getPackageUrls();
        if (purls.isEmpty()) {
            System.err.println("\nWARNING: Component '" + component + "' does not specify any packages");
            exportAnonymousPackage(bom, parent, component);
            return;
        }

        if (purls.size() > 1) {
            System.err.println("\nWARNING: Component '" + component + "' specifies " + purls.size() + " packages");
            final var pkg = exportAnonymousPackage(bom, parent, component);
            purls.stream()
                    .map(purl -> exportPackageIfNotExists(bom, component, purl, projectId, versionId))
                    .forEach(child -> exportRelation(bom, pkg, child, Relation.Type.DEPENDS_ON));
            return;
        }

        final var purl = purls.get(0);
        final var pkg = exportPackageIfNotExists(bom, component, purl, projectId, versionId);
        exportRelation(bom, parent, pkg, relationshipFor(component));
    }

    private Package exportPackageIfNotExists(BillOfMaterials bom, BlackDuckComponent component, PackageURL purl, UUID projectId, UUID versionId) {
        @NullOr Package pkg = packages.computeIfAbsent(purl, x -> {
            final var details = client.getComponentDetails(component);
            final var newPkg = new Package(purl)
                    .setSummary(component.getName());
            component.getLicense().ifPresent(newPkg::setConcludedLicense);
            details.getDescription().ifPresent(newPkg::setDescription);
            details.getHomepage().ifPresent(newPkg::setHomePage);
            bom.addPackage(newPkg);
            return newPkg;
        });

        final var dependencies = client.getDependencies(projectId, versionId, component);
        addChildren(bom, pkg, dependencies, projectId, versionId);
        return pkg;
    }

    private void exportRelation(BillOfMaterials bom, @NullOr Package parent, Package child, Relation.Type relationship) {
        if (parent != null) {
            bom.addRelation(parent, child, relationship);
        }
    }

    /**
     * @return strictest relationship for the component usages
     */
    Relation.Type relationshipFor(BlackDuckComponent component) {
        return component.getUsages().stream()
                .map(u -> USAGE_MAPPING.getOrDefault(u, Relation.Type.DEPENDS_ON))
                .min(Comparator.comparingInt(Enum::ordinal))
                .orElse(Relation.Type.DEPENDS_ON);
    }
}
