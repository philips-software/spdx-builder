/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.philips.research.spdxbuilder.core.bom.Package;
import com.philips.research.spdxbuilder.core.bom.Party;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;

public class OrtJson {
    RepositoryJson repository;
    AnalyzerJson analyzer;
}

class RepositoryJson {
    ConfigJson config;

    Set<PathMatcher> getExcludePaths() {
        return (config.excludes != null)
                ? config.excludes.getExcludePaths()
                : Set.of();
    }

    Set<PathMatcher> getExcludeScopes() {
        return (config.excludes != null)
                ? config.excludes.getExcludeScopes()
                : Set.of();
    }
}

class ConfigJson {
    ExcludeJson excludes;
}

class ExcludeJson {
    List<PatternJson> paths = new ArrayList<>();
    List<PatternJson> scopes = new ArrayList<>();

    Set<PathMatcher> getExcludePaths() {
        return paths.stream()
                .map(PatternJson::getGlob)
                .collect(Collectors.toSet());
    }

    Set<PathMatcher> getExcludeScopes() {
        return scopes.stream()
                .map(PatternJson::getGlob)
                .collect(Collectors.toSet());
    }
}

class PatternJson {
    private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

    String pattern;

    PathMatcher getGlob() {
        return FILE_SYSTEM.getPathMatcher("glob:" + pattern);
    }
}

class AnalyzerJson {
    ResultJson result;
}

class ResultJson {
    List<ProjectJson> projects;
    List<PackageWrapperJson> packages;
    boolean hasIssues;

    List<PackageJson> getPackages(Set<String> packageIdentifiers) {
        return packages.stream()
                .map(pkg -> pkg.pkg)
                .filter(pkg -> packageIdentifiers.contains(pkg.id))
                .collect(Collectors.toList());
    }
}

class PackageWrapperJson {
    @JsonProperty("package")
    PackageJson pkg;
}

abstract class PackageBaseJson {
    String id;

    DeclaredLicensesJson declaredLicensesProcessed;
    String description;
    URL homepageUrl;
    LocationJson sourceArtifact;

    public Package createPackage() {
        final var result = new Package(idElement(0), idElement(1), idElement(2), idElement(3));

        result.setSupplier(new Party(Party.Type.ORGANIZATION, result.getNamespace()));
        result.setDescription(description);
        result.setHomePage(homepageUrl);
        addSourceLocation(result);
        addDeclaredLicense(result);

        return result;
    }

    private String idElement(int index) {
        final var split = id.split(":");
        return (index < split.length) ? split[index] : "";
    }

    void addSourceLocation(Package pkg) {
        if (sourceArtifact != null) {
            sourceArtifact.addSourceLocation(pkg);
        }
    }

    void addDeclaredLicense(Package pkg) {
        if (declaredLicensesProcessed != null) {
            pkg.setDeclaredLicense(declaredLicensesProcessed.spdxExpression);
        }
    }
}

class DeclaredLicensesJson {
    String spdxExpression;
}

class ProjectJson extends PackageBaseJson {
    List<DependencyJson> scopes;
    File definitionFilePath;

    Set<String> getPackageIdentifiers(Set<PathMatcher> excludedScopes) {
        return scopes.stream()
                .filter(scope -> excludedScopes.stream()
                        .noneMatch(glob -> glob.matches(Path.of(scope.name))))
                .peek(scope -> System.out.println("- Adding scope '" + scope.name + "'"))
                .flatMap(scope -> scope.getAllDependencies().stream())
                .collect(Collectors.toSet());
    }
}

class PackageJson extends PackageBaseJson {
    VcsJson vcs_processed;

    @Override
    void addSourceLocation(Package pkg) {
        super.addSourceLocation(pkg);
        if (pkg.getLocation().isEmpty() && vcs_processed != null) {
            vcs_processed.addSourceLocation(pkg);
        }
    }
}

class DependencyJson {
    String id;
    String name;
    List<DependencyJson> dependencies = new ArrayList<>();

    Collection<String> getAllDependencies() {
        Set<String> result = new HashSet<>();
        for (var dep : dependencies) {
            result.add(dep.id);
            result.addAll(dep.getAllDependencies());
        }
        return result;
    }
}

class LocationJson {
    URI url;
    HashJson hash;

    public void addSourceLocation(Package pkg) {
        if (url != null && !url.toString().isEmpty()) {
            pkg.setLocation(url);
            if (hash != null) {
                hash.addHash(pkg);
            }
        }
    }
}

class HashJson {
    String value;
    String algorithm;

    void addHash(Package pkg) {
        if (value != null && !algorithm.isEmpty()) {
            pkg.addHash(algorithm, value);
        }
    }
}

class VcsJson {
    String type;
    URI url;
    String revision;
    String path;

    void addSourceLocation(Package pkg) {
        if (url == null || url.toString().isEmpty()) {
            return;
        }
        var location = url.toASCIIString();
        if (hasValue(type)) {
            location = type.toLowerCase() + "+" + location;
        }
        if (hasValue(revision)) {
            location += '@' + URLEncoder.encode(revision, StandardCharsets.UTF_8);
        } else if (hasValue(pkg.getVersion())) {
            location += '@' + URLEncoder.encode(pkg.getVersion(), StandardCharsets.UTF_8);
        }
        if (hasValue(path)) {
            location += '#' + URLEncoder.encode(path, StandardCharsets.UTF_8);
        }
        pkg.setLocation(URI.create(location));
    }

    private boolean hasValue(String string) {
        return (string != null && !string.isEmpty());
    }
}
