/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class OrtJson {
    RepositoryJson repository;
    AnalyzerJson analyzer;
}

class RepositoryJson {
    ConfigJson config;

    Set<String> getExcludeScopes() {
        if (config.excludes == null) {
            return Set.of();
        }
        return config.excludes.getExcludeScopes();
    }
}

class ConfigJson {
    ExcludeJson excludes;
}

class ExcludeJson {
    List<PatternJson> scopes = new ArrayList<>();

    Set<String> getExcludeScopes() {
        return scopes.stream().map(p -> p.pattern)
                .collect(Collectors.toSet());
    }
}

class PatternJson {
    String pattern;
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

    List<String> declaredLicenses;
    DeclaredLicensesJson declaredLicensesProcessed;
    String description;
    URL homepageUrl;

    public String getType() {
        return idElement(0);
    }

    public String getNamespace() {
        return idElement(1);
    }

    public String getName() {
        return idElement(2);
    }

    public String getVersion() {
        return idElement(3);
    }

    private String idElement(int index) {
        final var split = id.split(":");
        return (index < split.length) ? split[index] : "";
    }

    abstract LocationJson getSourceArtifact();

    String getSpdxLicense() {
        if (declaredLicensesProcessed == null) {
            return null;
        }
        return declaredLicensesProcessed.spdxExpression;
    }
}

class DeclaredLicensesJson {
    String spdxExpression;
}

class ProjectJson extends PackageBaseJson {
    NestedLocationJson sourceArtifact;
    List<DependencyJson> scopes;

    @Override
    LocationJson getSourceArtifact() {
        return sourceArtifact;
    }

    Set<String> getPackageIdentifiers(Set<String> excludedScopes) {
        return scopes.stream()
                .filter(scope -> !excludedScopes.contains(scope.name))
                .flatMap(scope -> scope.getAllDependencies().stream())
                .collect(Collectors.toSet());
    }
}

class PackageJson extends PackageBaseJson {
    NestedLocationJson sourceArtifact;

    @Override
    LocationJson getSourceArtifact() {
        return sourceArtifact;
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

abstract class LocationJson {
    URI url;

    abstract HashJson getHash();
}

class FlatLocationJson extends LocationJson {
    String hash;
    String hash_algorithm;

    @Override
    HashJson getHash() {
        return new HashJson(hash_algorithm, hash);
    }
}

class NestedLocationJson extends LocationJson {
    HashJson hash;

    @Override
    HashJson getHash() {
        return hash;
    }
}

class HashJson {
    String value;
    String algorithm;

    @SuppressWarnings("unused")
    public HashJson() {
    }

    public HashJson(String algorithm, String value) {
        this.algorithm = algorithm;
        this.value = value;
    }
}
