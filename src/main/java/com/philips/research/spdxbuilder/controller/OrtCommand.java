/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.domain.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.license_scanner.LicenseKnowledgeBase;
import com.philips.research.spdxbuilder.persistence.ort.OrtReader;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
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
@CommandLine.Command(name = "ort")
public class OrtCommand extends AbstractCommand {
    @Parameters(index = "0", description = "ORT Analyzer YAML file to read", paramLabel = "FILE", defaultValue = "analyzer-result.yml")
    @SuppressWarnings("NotNullFieldNotInitialized")
    File ortFile;

    @Option(names = {"--config", "-c"}, description = "Configuration YAML file", paramLabel = "FILE", defaultValue = ".spdx-builder.yml")
    @SuppressWarnings("NotNullFieldNotInitialized")
    File configFile;

    @Option(names = {"--scanner"}, description = "Add licenses from license scanner service", paramLabel = "SERVER_URL")
    @NullOr URI licenseScanner;

    @Override
    protected ConversionService createService() {
        final OrtReader reader = new OrtReader(ortFile);
        final BomWriter writer = new SpdxWriter(spdxFile);
        ConversionService service = licenseScanner != null
                ? new ConversionInteractor(reader, writer).setKnowledgeBase(new LicenseKnowledgeBase(licenseScanner))
                : new ConversionInteractor(reader, writer);

        final var config = readConfiguration();
        prepareReader(reader, config);
        prepareConversion(service, config);

        return service;
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

    private void prepareReader(OrtReader reader, Configuration config) {
        config.projects.forEach(project -> {
            reader.defineProjectPackage(project.id, project.purl);
            if (project.excluded != null) {
                reader.excludeScopes(project.id, project.excluded);
            }
        });
    }

    private void prepareConversion(ConversionService service, Configuration config) {
        service.setDocument(config.document.title, config.document.organization);
        service.setComment(config.document.comment);
        if (config.document.key != null) {
            service.setDocReference(config.document.key);
        }
        if (config.document.namespace != null) {
            service.setDocNamespace(config.document.namespace);
        }

        config.curations.forEach(curation -> {
            if (curation.license != null) {
                service.curatePackageLicense(curation.purl, curation.license);
            }
            if (curation.source != null) {
                service.curatePackageSource(curation.purl, curation.source);
            }
        });
    }
}
