/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;
import com.philips.research.spdxbuilder.core.bom.Party;
import com.philips.research.spdxbuilder.core.bom.Relation;
import pl.tlinkowski.annotation.basic.NullOr;

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
import java.util.stream.Stream;

public class OrtJson {
    @NullOr RepositoryJson repository;
    @NullOr AnalyzerJson analyzer;
}

class RepositoryJson {
    @NullOr ConfigJson config;

    Set<PathMatcher> getExcludePaths() {
        return (config != null && config.excludes != null)
                ? config.excludes.getExcludePaths()
                : Set.of();
    }

    Set<PathMatcher> getExcludeScopes() {
        return (config != null && config.excludes != null)
                ? config.excludes.getExcludeScopes()
                : Set.of();
    }
}

class ConfigJson {
    @NullOr ExcludeJson excludes;
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

    @NullOr String pattern;

    PathMatcher getGlob() {
        return FILE_SYSTEM.getPathMatcher("glob:" + pattern);
    }
}

class AnalyzerJson {
    @NullOr ResultJson result;
}

class ResultJson {
    List<ProjectJson> projects = new ArrayList<>();
    List<PackageWrapperJson> packages = new ArrayList<>();
    boolean hasIssues;

    Stream<PackageJson> packages() {
        return packages.stream().map(wrap -> wrap.pkg);
    }

    void removeProjects(Set<PathMatcher> excludedPaths) {
        projects.removeIf(p -> p.definitionFilePath == null
                || excludedPaths.stream()
                .anyMatch(glob -> glob.matches(p.definitionFilePath.toPath())));
    }
}

class PackageWrapperJson {
    @JsonProperty("package")
    @NullOr PackageJson pkg;
}

abstract class PackageBaseJson {
    @NullOr String id;
    @NullOr DeclaredLicensesJson declaredLicensesProcessed;
    @NullOr String description;
    @NullOr URI purl;
    @NullOr URL homepageUrl;
    @NullOr LocationJson sourceArtifact;

    Package createPackage() {
        final var result = new Package(idElement(0).toLowerCase(), idElement(1), idElement(2), idElement(3));

        result.setPurl(purl);
        result.setSupplier(new Party(Party.Type.ORGANIZATION, result.getNamespace()));
        result.setDescription(description);
        result.setHomePage(homepageUrl);
        addSourceLocation(result);
        addDeclaredLicense(result);

        return result;
    }

    private String idElement(int index) {
        if (id == null) {
            return "";
        }
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
    @NullOr String spdxExpression;
}

class ProjectJson extends PackageBaseJson {
    List<DependencyJson> scopes = new ArrayList<>();
    @NullOr File definitionFilePath;

    public void removeScopes(Set<PathMatcher> excludedScopes) {
        scopes.removeIf(scope -> excludedScopes.stream()
                .anyMatch(glob -> glob.matches(Path.of(scope.name))));
    }
}

class PackageJson extends PackageBaseJson {
    @NullOr VcsJson vcs_processed;

    @Override
    void addSourceLocation(Package pkg) {
        super.addSourceLocation(pkg);
        if (pkg.getLocation().isEmpty() && vcs_processed != null) {
            vcs_processed.addSourceLocation(pkg);
        }
    }
}

class DependencyJson {
    static final Map<String, Relation.Type> LINKAGE = new HashMap<>();

    static {
        LINKAGE.put("DYNAMIC", Relation.Type.DYNAMIC_LINK);
        LINKAGE.put("STATIC", Relation.Type.STATIC_LINK);
        LINKAGE.put("PROJECT_DYNAMIC", Relation.Type.DYNAMIC_LINK);
        LINKAGE.put("PROJECT_STATIC", Relation.Type.STATIC_LINK);
    }

    @NullOr String id;
    @NullOr String name;
    String linkage = "DYNAMIC";
    List<DependencyJson> dependencies = new ArrayList<>();

    void putAllDependencies(Map<String, Package> dictionary) {
        for (DependencyJson dep : dependencies) {
            if (dep.id != null) {
                //noinspection ConstantConditions
                dictionary.put(dep.id, null);
                dep.putAllDependencies(dictionary);
            }
        }
    }

    void registerRelations(BillOfMaterials bom, Package from, Map<String, Package> dictionary) {
        final var me = dictionary.get(id);
        if (me == null) {
            return;
        }
        //noinspection ConstantConditions
        if (from != null) {
            bom.addRelation(from, me, LINKAGE.getOrDefault(linkage, Relation.Type.DEPENDS_ON));
        }
        for (var dep : dependencies) {
            dep.registerRelations(bom, me, dictionary);
        }
    }
}

class LocationJson {
    @NullOr URI url;
    @NullOr HashJson hash;

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
    @NullOr String value;
    @NullOr String algorithm;

    void addHash(Package pkg) {
        if (value != null && algorithm != null && !algorithm.isEmpty()) {
            pkg.addHash(algorithm, value);
        }
    }
}

class VcsJson {
    @NullOr String type;
    @NullOr URI url;
    @NullOr String revision;
    @NullOr String path;

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

    private boolean hasValue(@NullOr String string) {
        return (string != null && !string.isEmpty());
    }
}
