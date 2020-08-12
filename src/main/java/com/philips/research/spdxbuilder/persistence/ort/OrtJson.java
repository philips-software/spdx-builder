/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class OrtJson {
    AnalyzerJson analyzer;
}

class AnalyzerJson {
    ResultJson result;
}

class ResultJson {
    List<ProjectJson> projects;
    List<PackageWrapperJson> packages;
    boolean hasIssues;

    List<PackageJson> getPackages() {
        return packages.stream().map(pkg -> pkg.pkg).collect(Collectors.toList());
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
        return declaredLicensesProcessed.spdxExpression;
    }
}

class DeclaredLicensesJson {
    String spdxExpression;
}

class ProjectJson extends PackageBaseJson {
    NestedLocationJson sourceArtifact;

    @Override
    LocationJson getSourceArtifact() {
        return sourceArtifact;
    }
}

class PackageJson extends PackageBaseJson {
    NestedLocationJson sourceArtifact;

    @Override
    LocationJson getSourceArtifact() {
        return sourceArtifact;
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
