/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.persistence.BillOfMaterialsStore;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * OSS Review Toolkit (ORT) YAML file reader.
 *
 * @see <a href="https://github.com/oss-review-toolkit/ort">OSS Review Toolkit</a>
 */
public class OrtReader implements BillOfMaterialsStore {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    /**
     * Parses the ORT YAML file that was output by the ORT Analyzer.
     *
     * @param file
     * @return reconstructed bill-of-materials
     */
    public BillOfMaterials read(File file) {
        try {
            final var yaml = MAPPER.readValue(file, OrtJson.class);
            final var excludedPaths = yaml.repository.getExcludePaths();
            final var excludedScopes = yaml.repository.getExcludeScopes();
            final var result = yaml.analyzer.result;

            //TODO Break if Analyzer failed

            final var bom = new BillOfMaterials();
            final var identifiers = new HashSet<String>();
            result.projects.stream()
                    .filter(p -> p.definitionFilePath != null)
                    .filter(p -> excludedPaths.stream()
                            .noneMatch(glob -> glob.matches(p.definitionFilePath.toPath())))
                    .peek(p -> System.out.println("Adding project from '" + p.definitionFilePath + "'"))
                    .forEach(p -> {
                        bom.addProject(p.createPackage());
                        identifiers.addAll(p.getPackageIdentifiers(excludedScopes));
                    });
            for (PackageJson pkg : result.getPackages(identifiers)) {
                bom.addDependency(pkg.createPackage());
            }
            System.out.println("Found " + bom.getDependencies().size() + " unique packages");
            return bom;
        } catch ( IOException e) {
            //TODO needs a business exception
            throw new RuntimeException(e);
        }

    }

    public void write(File file, BillOfMaterials bom) {
        // Not implemented
    }
}
