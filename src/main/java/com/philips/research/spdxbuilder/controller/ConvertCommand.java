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

import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.ConversionInteractor;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.ConversionStore;
import com.philips.research.spdxbuilder.persistence.ConversionPersistence;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * CLI command to generate an SPDX file from an ORT Analyzer YAML.
 */
@CommandLine.Command(name = "spdx-builder", mixinStandardHelpOptions = true, version = "1.0")
public class ConvertCommand implements Runnable {
    @Option(names = {"--ort", "-i"}, description = "Read ORT Analyzer YAML file", descriptionKey = "file")
    @NullOr File ortFile;
    @Option(names = {"--config", "-c"}, description = "Configuration YAML file", descriptionKey = "file", defaultValue = ".spdx.yml")
    @SuppressWarnings("NotNullFieldNotInitialized")
    File configFile;
    @Option(names = {"--scanner"}, description = "Add licenses from license scanner service", descriptionKey = "server url")
    @NullOr URI licenseScanner;
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Option(names = {"--output", "-o"}, description = "Output SPDX tag-value file", descriptionKey = "file", defaultValue = "bom.spdx")
    File spdxFile;

    @SuppressWarnings("ConstantConditions")
    final ConversionStore store = new ConversionPersistence(licenseScanner);
    final ConversionService service = new ConversionInteractor(store);

    @Override
    public void run() {
        readConfiguration();

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

    private void readConfiguration() {
        try (final var stream = new FileInputStream(configFile)) {
            final var config = Configuration.parse(stream);
            service.setDocument(config.document.title, config.document.organization);
            service.setComment(config.document.comment);
            if (config.document.spdxId != null) {
                service.setDocReference(config.document.spdxId);
            }
            if (config.document.namespace != null) {
                service.setDocNamespace(config.document.namespace);
            }
            config.projects.forEach(project -> service.defineProjectPackage(project.id, project.purl));
            config.curations.forEach(curation -> {
                if (curation.license != null) {
                    service.curatePackageLicense(curation.purl, curation.license);
                }
                if (curation.source != null) {
                    service.curatePackageSource(curation.purl, curation.source);
                }
            });
        } catch (IOException e) {
            System.out.println("Configuration error: " + e.getMessage());
            System.out.println("Supported YAML configuration file format is:");
            System.out.println(Configuration.example());

            throw new BusinessException("Failed to read configuration");
        }
    }
}
