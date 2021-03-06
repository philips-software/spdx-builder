/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.*;

/**
 * OSS Review Toolkit (ORT) YAML file reader.
 *
 * @see <a href="https://github.com/oss-review-toolkit/ort">OSS Review Toolkit</a>
 */
public class OrtReader implements BomReader {
    private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final File file;
    private final Map<String, @NullOr URI> projectPackages = new HashMap<>();
    private final Map<String, List<String>> projectExcludes = new HashMap<>();

    public OrtReader(File file) {
        this.file = file;
    }

    /**
     * Marks a project for import with an (optional) package alias
     *
     * @param id   ORT identifier for the project
     * @param purl (Optional) Package URL
     */
    public OrtReader defineProjectPackage(String id, @NullOr URI purl) {
        //noinspection ConstantConditions
        projectPackages.put(id, purl);
        return this;
    }

    /**
     * Adds excluded scopes to ORT.
     *
     * @param id       ORT identifier for the project
     * @param excluded (optionally wild-carded) scope names
     */
    public OrtReader excludeScopes(String id, List<String> excluded) {
        projectExcludes.put(id, excluded);
        return this;
    }

    @Override
    public void read(BillOfMaterials bom) {
        System.out.println("Reading ORT file '" + file + "'...");
        try {
            final var yaml = MAPPER.readValue(file, OrtJson.class);
            final var dictionary = new HashMap<String, Package>();

            if (yaml.analyzer == null || yaml.analyzer.result == null) {
                throw new OrtReaderException("ORT file does not include an 'analyzer.result' section");
            }
            if (yaml.analyzer.result.hasIssues) {
                throw new OrtReaderException("The analyzed ORT file has issues");
            }
            final var result = yaml.analyzer.result;

            printProjects(result, projectPackages.keySet());
            cleanupYaml(yaml, projectPackages, projectExcludes);
            registerProjects(result, bom, dictionary);
            registerPackages(result, bom, dictionary);
            registerRelations(result, bom, dictionary);

            System.out.println();
            System.out.println("Found " + bom.getPackages().size() + " unique packages");
        } catch (IOException e) {
            throw new BusinessException("Failed to read ORT file: " + e);
        }
    }

    private void printProjects(ResultJson result, Set<String> projectIds) {
        System.out.println("Detected " + result.projects.size() + " project(s):");
        result.projects.forEach(project -> {
            final var tick = projectIds.contains(project.id) ? "+" : "-";
            final var from = (project.definitionFilePath != null) ? " from '" + project.definitionFilePath + "'" : "";
            System.out.println(tick + " '" + project.id + "'" + from);
        });
    }

    private void cleanupYaml(OrtJson yaml, Map<String, URI> projectPackages, Map<String, List<String>> projectExcludes) {
        if (yaml.repository == null) {
            return;
        }
        //noinspection ConstantConditions
        final var result = yaml.analyzer.result;
        final var excludedPaths = yaml.repository.getExcludePaths();
        final var excludedScopes = yaml.repository.getExcludeScopes();

        assert result != null;
        result.removeProjects(excludedPaths);
        result.keepProjects(projectPackages.keySet());
        result.updateProjectPackages(projectPackages);
        result.projects.forEach(p -> {
            final var globs = new HashSet<>(excludedScopes);
            projectExcludes.getOrDefault(p.id, List.of()).stream()
                    .map(pattern -> FILE_SYSTEM.getPathMatcher("glob:" + pattern))
                    .forEach(globs::add);
            p.removeScopes(globs);
        });
    }

    private void registerProjects(ResultJson result, BillOfMaterials bom, HashMap<String, Package> dictionary) {
        result.projects.forEach(p -> {
            if (p.id == null) {
                return;
            }
            System.out.println();
            System.out.println("Adding project '" + p.id + "':");
            var project = p.createPackage();
            dictionary.put(p.id, project);
            bom.addPackage(project);
            p.scopes.forEach(scope -> {
                System.out.println("+ Adding scope '" + scope.name + "'");
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
}
