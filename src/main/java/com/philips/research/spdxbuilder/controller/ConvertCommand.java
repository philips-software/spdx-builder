/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.ConversionPersistence;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.net.URI;

/**
 * CLI command to generate an SPDX file from an ORT Analyzer YAML.
 */
@CommandLine.Command(name = "spdx-builder", mixinStandardHelpOptions = true, version = "1.0")
public class ConvertCommand implements Runnable {
    @Option(names = "--ort", description = "Read ORT Analyzer YAML file", descriptionKey = "file")
    File ortFile;

    File spdxFile = new File("bom.spdx");

    @Option(names = "--scanner", description = "Add licenses from license scanner service", descriptionKey = "server url")
    URI licenseScanner;

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
        service.writeBillOfMaterials(spdxFile);
    }
}
