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
import com.philips.research.spdxbuilder.core.bom.Package;
import com.philips.research.spdxbuilder.persistence.BillOfMaterialsStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
     * @return reconstructed bill-of-materials
     */
    public BillOfMaterials read(File file) {
        try {
            final var yaml = MAPPER.readValue(file, OrtJson.class);

            //TODO Break if Analyzer reported failures

            final var bom = new BillOfMaterials();
            final var dictionary = new HashMap<String, Package>();
            if (yaml.analyzer == null || yaml.analyzer.result == null) {
                throw new OrtReaderException("ORT file does not include an 'analyzer.result' section");
            }
            final var result = yaml.analyzer.result;

            cleanupYaml(yaml);
            registerProjects(result, bom, dictionary);
            registerPackages(result, bom, dictionary);
            registerRelations(result, bom, dictionary);

            System.out.println("Found " + bom.getPackages().size() + " unique packages in " + bom.getProjects().size() + " projects");

            return bom;
        } catch (IOException e) {
            //TODO needs a business exception
            throw new RuntimeException(e);
        }
    }

    private void cleanupYaml(OrtJson yaml) {
        if (yaml.repository == null) {
            return;
        }
        //noinspection ConstantConditions
        final var result = yaml.analyzer.result;
        final var excludedPaths = yaml.repository.getExcludePaths();
        final var excludedScopes = yaml.repository.getExcludeScopes();

        assert result != null;
        result.removeProjects(excludedPaths);
        result.projects.forEach(p -> p.removeScopes(excludedScopes));
    }

    private void registerProjects(ResultJson result, BillOfMaterials bom, HashMap<String, Package> dictionary) {
        result.projects.forEach(p -> {
            if (p.id == null) {
                return;
            }
            System.out.println("Adding project from '" + p.definitionFilePath + "'");
            var project = p.createPackage();
            dictionary.put(p.id, project);
            bom.addProject(project);
            p.scopes.forEach(scope -> {
                System.out.println("- Adding scope '" + scope.name + "'");
                scope.putAllDependencies(dictionary);
            });
        });
    }

    private void registerPackages(ResultJson result, BillOfMaterials bom, HashMap<String, Package> dictionary) {
        result.packages()
                .filter(pkg -> dictionary.containsKey(pkg.id))
                .forEach(pkg -> {
                    if (pkg.id == null) {
                        return;
                    }
                    final var created = pkg.createPackage();
                    dictionary.put(pkg.id, created);
                    bom.addPackage(created);
                });
    }

    private void registerRelations(ResultJson result, BillOfMaterials bom, HashMap<String, Package> dictionary) {
        result.projects.forEach(p -> {
            final var project = dictionary.get(p.id);
            p.scopes.stream()
                    .flatMap(scope -> scope.dependencies.stream())
                    .forEach(dep -> dep.registerRelations(bom, project, dictionary));
        });
    }

    public void write(File file, BillOfMaterials bom) {
        // Not implemented
    }
}
