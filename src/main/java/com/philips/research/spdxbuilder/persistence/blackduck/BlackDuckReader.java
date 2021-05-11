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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlackDuckReader implements BomReader {
    private static final Map<String, Relation.Type> USAGE_MAPPING = Map.of(
            "SOURCE_CODE", Relation.Type.DESCENDANT_OF,
            "STATICALLY_LINKED", Relation.Type.STATIC_LINK,
            "DYNAMICALLY_LINKED", Relation.Type.DYNAMIC_LINK);

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

        exportProjectVersion(bom);

        System.out.println("Black Duck export finished");
    }

    private void exportProjectVersion(BillOfMaterials bom) {
        exportProjectMetadata(bom);
        System.out.println("Exporting Black Duck project '" + project.getName() + "', version '" + projectVersion.getName() + "'");
        exportProjectComponents(bom);
        System.out.println("done");
    }

    private void exportProjectMetadata(BillOfMaterials bom) {
        final var serverVersion = client.getServerVersion();
        bom.setComment(String.format("Extracted from Black Duck server version %s", serverVersion));

        project = client.findProject(projectName)
                .orElseThrow(() -> new BlackDuckException("Found no project named '" + projectName + "'"));
        projectVersion = client.findProjectVersion(project.getId(), versionName)
                .orElseThrow(() -> new BlackDuckException("Found no version named '" + versionName + "' in project '" + project.getName()));

        bom.setTitle(project.getName() + " " + projectVersion.getName());
    }

    private void exportProjectComponents(BillOfMaterials bom) {
        System.out.print("Building tree of components ");
        final var root = new Package("", project.getName(), projectVersion.getName());
        project.getDescription().ifPresent(root::setDescription);
        projectVersion.getDescription().ifPresent(root::setSummary);
        projectVersion.getLicense().ifPresent(root::setDeclaredLicense);
        bom.addPackage(root);

        final var components = client.getComponents(project.getId(), projectVersion.getId());
        addChildren(bom, root, components);
    }

    void addChildren(BillOfMaterials bom, @NullOr Package parent, List<BlackDuckComponent> components) {
        components.forEach(component -> {
            final var purls = component.getPackageUrls();
            if (purls.isEmpty()) {
                System.err.println("\nWARNING: Skipped component '" + component + "' as it does not specify any packages");
            }
            purls.stream()
                    .map(purl -> exportPackageIfNotExists(bom, component, purl))
                    .forEach(pkg -> exportRelation(bom, parent, pkg, component));
            System.out.print(".");
        });
    }

    private Package exportPackageIfNotExists(BillOfMaterials bom, BlackDuckComponent component, PackageURL purl) {
        final var existing = packages.get(purl);
        if (existing != null) {
            return existing;
        }

        final var details = client.getComponentDetails(component);
        final var pkg = new Package(purl)
                .setConcludedLicense(component.getLicense())
                .setSummary(component.getName());
        details.getDescription().ifPresent(pkg::setDescription);
        details.getHomepage().ifPresent(pkg::setHomePage);
        bom.addPackage(pkg);
        packages.put(purl, pkg);

        addChildren(bom, pkg, client.getDependencies(project.getId(), projectVersion.getId(), component));
        return pkg;
    }

    private void exportRelation(BillOfMaterials bom, @NullOr Package parent, Package child, BlackDuckComponent component) {
        if (parent != null) {
            bom.addRelation(parent, child, relationshipFor(component));
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
