/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.ConversionPersistence;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;

/**
 * CLI command to generate an SPDX file from an ORT Analyzer YAML.
 */
@CommandLine.Command(name = "spdx-builder", mixinStandardHelpOptions = true, version = "1.0")
public class ConvertCommand implements Runnable {
    @Option(names = {"--ort", "-i"}, description = "Read ORT Analyzer YAML file", descriptionKey = "file")
    @NullOr File ortFile;


    @Option(names = {"--scanner"}, description = "Add licenses from license scanner service", descriptionKey = "server url")
    @NullOr URI licenseScanner;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @Option(names = {"--output", "-o"}, description = "Output SPDX tag-value file", descriptionKey = "file", defaultValue = "bom.spdx")
    File spdxFile;

    @Override
    public void run() {
        final var store = new ConversionPersistence(licenseScanner);
        final var service = new ConversionInteractor(store);

        if (ortFile != null) {
            service.readOrtAnalysis(ortFile);
        }
        if (licenseScanner != null) {
            service.scanLicenses();
        }
        if (!spdxFile.getName().contains(".")) {
            spdxFile = new File(spdxFile.getPath() + ".spdx");
        }
        service.writeBillOfMaterials(spdxFile);
    }
}
