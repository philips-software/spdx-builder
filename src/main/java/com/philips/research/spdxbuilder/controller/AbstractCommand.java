/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.ConversionService;
import picocli.CommandLine;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;

public abstract class AbstractCommand implements Runnable {
    @CommandLine.Option(names = {"--version", "-V"}, description = "Show version info and exit")
    boolean showVersion;

    @CommandLine.Option(names = {"--help", "-H"}, usageHelp = true, description = "Show this message and exit")
    @SuppressWarnings("unused")
    boolean showUsage;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @CommandLine.Option(names = {"--output", "-o"}, description = "Output SPDX tag-value file", paramLabel = "FILE", defaultValue = "bom.spdx")
    File spdxFile;

    @CommandLine.Option(names = {"--upload"}, description = "Upload SPDX file", paramLabel = "SERVER_URL")
    @NullOr URI uploadUrl;

    @Override
    public void run() {
        if (showVersion) {
            final var app = getClass().getPackage().getImplementationTitle();
            final var version = getClass().getPackage().getImplementationVersion();
            System.out.println(app + ", Version " + version);
            return;
        }

        execute();
    }

    abstract protected void execute();

    File writeResult(ConversionService service) {
        if (!spdxFile.getName().contains(".")) {
            spdxFile = new File(spdxFile.getPath() + ".spdx");
        }
        System.out.println("Writing SBOM to '" + spdxFile +"'");
        service.writeBillOfMaterials(spdxFile);
        return spdxFile;
    }

    void upload(File file) {
        if (uploadUrl == null) {
            return;
        }
        System.out.println("Uploading '" + file.getName() + "' to " + uploadUrl);
        new UploadClient(uploadUrl).upload(file);
    }
}
