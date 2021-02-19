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
import picocli.CommandLine.Option;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;

/**
 * Shared generic part of CLI commands.
 */
public abstract class AbstractCommand implements Runnable {
    @Option(names = {"--version", "-V"}, description = "Show version info and exit")
    boolean showVersion;

    @Option(names = {"--help", "-H"}, usageHelp = true, description = "Show this message and exit")
    @SuppressWarnings("unused")
    boolean showUsage;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @Option(names = {"--output", "-o"}, description = "Output SPDX tag-value file", paramLabel = "FILE", defaultValue = "bom.spdx")
    File spdxFile;

    @Option(names = {"--upload"}, description = "Upload SPDX file", paramLabel = "SERVER_URL")
    @NullOr URI uploadUrl;

    /**
     * @return instantiated service for the provided parameters and options
     */
    abstract protected ConversionService createService();

    @Override
    public void run() {
        if (showVersion) {
            final var app = getClass().getPackage().getImplementationTitle();
            final var version = getClass().getPackage().getImplementationVersion();
            System.out.println(app + ", Version " + version);
            System.exit(0);
        }

        if (!spdxFile.getName().contains(".")) {
            spdxFile = new File(spdxFile.getPath() + ".spdx");
        }

        createService().convert();

        if (uploadUrl != null) {
            System.out.println("Uploading '" + spdxFile.getName() + "' to " + uploadUrl);
            new UploadClient(uploadUrl).upload(spdxFile);
        }

        System.exit(0);
    }
}
