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

import java.net.URL;

public class BlackDuckReader implements BomReader {
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
        bom.setComment(String.format("Project '%s' version '%s' from Black Duck server version %s", projectName, versionName, serverVersion));
        final var project = client.findProject(projectName)
                .orElseThrow(() -> new BlackDuckException("Found no project named '" + projectName + "'"));
        final var projectVersion = client.findProjectVersion(project.getId(), versionName)
                .orElseThrow(() -> new BlackDuckException("Found no version named '" + versionName + "' in project '" + project.name));

        bom.setTitle(project.name + " " + projectVersion.versionName);

        System.out.println("Reading components...");
        client.getComponents(project.getId(), projectVersion.getId()).forEach(c -> {
            System.out.println("Component " + c.componentName + " " + c.componentVersionName);
        });
    }
}
