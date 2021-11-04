/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.persistence.tree.TreeWriter;
import picocli.CommandLine.Option;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    FileOutputStream spdxStream;

    @Option(names = {"--tree"}, description = "Print dependency tree")
    boolean printTree;

    @Option(names = {"--upload"}, description = "Upload SPDX file", paramLabel = "SERVER_URL")
    @NullOr URI uploadUrl;

    @Option(names = {"--force"}, description = "Create output if metadata is incomplete")
    boolean forceContinue;

    /**
     * @return instantiated service for the provided parameters and options
     */
    abstract protected ConversionService createService();

    @Override
    public void run() {
        showBanner();

        if (showVersion) {
            final var app = getClass().getPackage().getImplementationTitle();
            final var version = getClass().getPackage().getImplementationVersion();
            System.out.println(app + ", Version " + version);
            System.exit(0);
        }

        if (!spdxFile.getName().contains(".")) {
            spdxFile = new File(spdxFile.getPath() + ".spdx");
            System.out.println("Writing SBOM to '" + spdxFile.getName() + "'");
            try {
                spdxStream = new FileOutputStream(spdxFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        final var service = createService();
        service.read();
        if (printTree) {
            service.apply(new TreeWriter());
        }
        service.convert(forceContinue);

        if (uploadUrl != null) {
            System.out.println("Uploading '" + spdxFile.getName() + "' to " + uploadUrl);
            new UploadClient(uploadUrl).upload(spdxFile);
        }

        System.exit(0);
    }

    private void showBanner() {
        System.out.println(" ___ ___ _____  __   ___      _ _    _         ");
        System.out.println("/ __| _ \\   \\ \\/ /__| _ )_  _(_) |__| |___ _ _ ");
        System.out.println("\\__ \\  _/ |) >  <___| _ \\ || | | / _` / -_) '_|");
        System.out.println("|___/_| |___/_/\\_\\  |___/\\_,_|_|_\\__,_\\___|_|");
    }
}
