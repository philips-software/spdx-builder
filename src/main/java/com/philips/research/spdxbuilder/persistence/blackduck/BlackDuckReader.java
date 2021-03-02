/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;

import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlackDuckReader implements BomReader {
    private static final Map<String, Relation.Type> USAGE_MAPPING = Map.of(
            "SOURCE_CODE", Relation.Type.DESCENDANT_OF,
            "STATICALLY_LINKED", Relation.Type.STATIC_LINK,
            "DYNAMICALLY_LINKED", Relation.Type.DYNAMIC_LINK);

    private final BlackDuckClient client;
    private final String token;
    private final String projectName;
    private final String versionName;

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

        exportProjectInfo(bom);

        System.out.println("Black Duck export finished");
    }

    private void exportProjectInfo(BillOfMaterials bom) {
        final var serverVersion = client.getServerVersion();
        bom.setComment(String.format("Project '%s', version '%s' from Black Duck server version %s", projectName, versionName, serverVersion));

        final var project = client.findProject(projectName)
                .orElseThrow(() -> new BlackDuckException("Found no project named '" + projectName + "'"));
        final var projectVersion = client.findProjectVersion(project.getId(), versionName)
                .orElseThrow(() -> new BlackDuckException("Found no version named '" + versionName + "' in project '" + project.getName()));

        System.out.println("Exporting Black Duck project '" + project.getName() + "', version '" + projectVersion.getName() + "'...");
        bom.setTitle(project.getName() + " " + projectVersion.getName());

        final var packages = new HashMap<UUID, Package>();
        final var components = new HashMap<UUID, BlackDuckComponent>();
        System.out.println("Building list of components...");
        client.getComponents(project.getId(), projectVersion.getId()).forEach(c -> {
            final var purls = c.getPackageUrls();
            if (purls.isEmpty()) {
                System.err.println("WARNING: Component '" + c + "' does not specify any packages");
            }
            c.getPackageUrls().forEach(purl -> {
                final var pkg = new Package(purl.getType(), purl.getNamespace(), purl.getName(), purl.getVersion())
                        .setSummary(c.getName());
                packages.put(c.getVersionId(), pkg);
                components.put(c.getVersionId(), c);
            });
        });

        packages.values().forEach(bom::addPackage);

        System.out.print("Analyzing dependencies");
        components.values().forEach(c -> {
            System.out.print(".");
            final var dependencies = client.getDependencies(project.getId(), projectVersion.getId(), c);
            dependencies.stream()
                    .filter(dep -> components.containsKey(dep.getVersionId()))
                    .forEach(dep -> {
                        final var from = packages.get(c.getVersionId());
                        final var to = packages.get(dep.getVersionId());
                        final var relationship = relationshipFor(components.get(dep.getVersionId()));
                        bom.addRelation(from, to, relationship);
                    });
        });
        System.out.println("done");
    }

    /**
     * @return strictest relationship for the listed component usages
     */
    Relation.Type relationshipFor(BlackDuckComponent component) {
        return component.getUsages().stream()
                .map(u -> USAGE_MAPPING.getOrDefault(u, Relation.Type.DEPENDS_ON))
                .min(Comparator.comparingInt(Enum::ordinal))
                .orElse(Relation.Type.DEPENDS_ON);
    }
}
