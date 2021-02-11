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
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ComponentJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.OriginJson;

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

    public BlackDuckReader(URL url, String token, String projectName, String versionName) {
        this(new BlackDuckClient(url), token, projectName, versionName);
    }

    BlackDuckReader(BlackDuckClient client, String token, String projectName, String versionName) {
        this.client = client;
        this.token = token;
        this.projectName = projectName;
        this.versionName = versionName;
    }

    @Override
    public void read(BillOfMaterials bom) {
        System.out.println("Exporting Black Duck project '" + projectName + "', version '" + versionName + "'...");

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
                .orElseThrow(() -> new BlackDuckException("Found no version named '" + versionName + "' in project '" + project.name));

        bom.setTitle(project.name + " " + projectVersion.versionName);

        final var components = new HashMap<UUID, ComponentJson>();
        final var origins = new HashMap<UUID, OriginJson>();
        final var packages = new HashMap<UUID, Package>();
        System.out.println("Building list of components...");
        client.getComponents(project.getId(), projectVersion.getId()).stream()
                .filter(c -> !c.ignored)
                .forEach(c -> {
                    if (c.origins.isEmpty()) {
                        System.out.println("WARNING: Component " + c + " does not specify any origin");
                    }
                    c.origins.forEach(origin -> {
                        final var pkg = new Package(origin.getType(), origin.getNamespace(), origin.getName(), origin.getVersion())
                                .setSummary(c.componentName);
                        final var uuid = origin.getId();
                        components.put(uuid, c);
                        origins.put(uuid, origin);
                        packages.put(uuid, pkg);
                    });
                });

        packages.values().forEach(bom::addPackage);

        System.out.print("Analyzing dependencies");
        origins.forEach((originId, origin) -> {
            System.out.print(".");
            final var dependencies = client.getDependencies(origin);
            dependencies.stream()
                    .filter(dep -> origins.containsKey(dep.getId()))
                    .forEach(dep -> {
                        final var from = packages.get(originId);
                        final var to = packages.get(dep.getId());
                        final var relationship = relationshipFor(components.get(originId));
                        bom.addRelation(from, to, relationship);
                    });
        });
    }

    /**
     * @return strictest relationship for the listed component usages
     */
    Relation.Type relationshipFor(ComponentJson component) {
        return component.usages.stream()
                .map(u -> USAGE_MAPPING.getOrDefault(u, Relation.Type.DEPENDS_ON))
                .min(Comparator.comparingInt(Enum::ordinal))
                .orElse(Relation.Type.DEPENDS_ON);
    }
}
