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
import picocli.CommandLine.Parameters;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * CLI command to generate an SPDX file from an ORT Analyzer YAML.
 */
@CommandLine.Command(name = "spdx-builder")
public class ConvertCommand implements Runnable {
    @Option(names = {"--version", "-V"}, description = "Show version info and exit")
    boolean showVersion;
    @Option(names = {"--help", "-H"}, usageHelp = true, description = "Show this message exit")
    @SuppressWarnings("unused")
    boolean showUsage;
    @Option(names = {"--config", "-c"}, description = "Configuration YAML file", paramLabel = "FILE", defaultValue = ".spdx-builder.yml")
    @SuppressWarnings("NotNullFieldNotInitialized")
    File configFile;
    @Option(names = {"--scanner"}, description = "Add licenses from license scanner service", paramLabel = "SERVER_URL")
    @NullOr URI licenseScanner;
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Option(names = {"--output", "-o"}, description = "Output SPDX tag-value file", paramLabel = "FILE", defaultValue = "bom.spdx")
    File spdxFile;
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Parameters(index = "0", description = "ORT Analyzer YAML file to read", paramLabel = "FILE", defaultValue = "analyzer-result.yml")
    File ortFile;

    @SuppressWarnings("ConstantConditions")
    final ConversionStore store = new ConversionPersistence(licenseScanner);
    final ConversionService service = new ConversionInteractor(store);

    @Override
    public void run() {
        if (showVersion) {
            final var app = getClass().getPackage().getImplementationTitle();
            final var version = getClass().getPackage().getImplementationVersion();
            System.out.println(app + ", Version " + version);
            return;
        }

        final var config = readConfiguration();
        prepare(config);
        readInput();
        scan();
        curate(config);
        writeResult();
    }


    private Configuration readConfiguration() {
        try (final var stream = new FileInputStream(configFile)) {
            return Configuration.parse(stream);

        } catch (IOException e) {
            System.out.println("Configuration error: " + e.getMessage());
            System.out.println("Supported YAML configuration file format is:");
            System.out.println(Configuration.example());

            throw new BusinessException("Failed to read configuration");
        }
    }

    private void prepare(Configuration config) {
        service.setDocument(config.document.title, config.document.organization);
        service.setComment(config.document.comment);
        if (config.document.key != null) {
            service.setDocReference(config.document.key);
        }
        if (config.document.namespace != null) {
            service.setDocNamespace(config.document.namespace);
        }

        config.projects.forEach(project -> {
            service.defineProjectPackage(project.id, project.purl);
            if (project.excluded != null) {
                service.excludeScopes(project.id, project.excluded);
            }
        });
    }

    private void readInput() {
        service.readOrtAnalysis(ortFile);
    }

    private void scan() {
        if (licenseScanner != null) {
            service.scanLicenses();
        }
    }

    private void curate(Configuration config) {
        config.curations.forEach(curation -> {
            if (curation.license != null) {
                service.curatePackageLicense(curation.purl, curation.license);
            }
            if (curation.source != null) {
                service.curatePackageSource(curation.purl, curation.source);
            }
        });
    }

    private void writeResult() {
        if (!spdxFile.getName().contains(".")) {
            spdxFile = new File(spdxFile.getPath() + ".spdx");
        }
        service.writeBillOfMaterials(spdxFile);
    }
}
