/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Single bill-of-materials package.
 */
public final class Package {
    private final String type;
    private final String namespace;
    private final String name;
    private final String version;
    private final Map<String, String> hash = new HashMap<>();
    private @NullOr PackageURL purl;
    private @NullOr Party supplier;
    private @NullOr Party originator;
    private @NullOr String filename;
    private @NullOr URI sourceLocation;
    private @NullOr URL homePage;
    private @NullOr License concludedLicense;
    private @NullOr License declaredLicense;
    private @NullOr License detectedLicense;
    private @NullOr String copyright;
    private @NullOr String summary;
    private @NullOr String description;
    private @NullOr String attribution;

    public Package(String type, @NullOr String namespace, String name, String version) {
        this.type = type;
        this.namespace = (namespace != null) ? namespace : "";
        this.name = name;
        this.version = version;
    }

    public static Package fromPurl(PackageURL purl) {
        return new Package(purl.getType(), purl.getNamespace(), purl.getName(), purl.getVersion());
    }

    public String getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public PackageURL getPurl() {
        if (purl == null) {
            return implicitPurl();
        }
        return purl;
    }

    private PackageURL implicitPurl() {
        try {
            return PackageURLBuilder.aPackageURL()
                    .withType(type)
                    .withNamespace(namespace)
                    .withName(name)
                    .withVersion(version)
                    .build();
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Package setPurl(PackageURL purl) {
        this.purl = purl;
        return this;
    }

    public Optional<Party> getOriginator() {
        return Optional.ofNullable(originator);
    }

    public Package setOriginator(@NullOr Party originator) {
        this.originator = originator;
        return this;
    }

    public Optional<Party> getSupplier() {
        return Optional.ofNullable(supplier);
    }

    public Package setSupplier(@NullOr Party supplier) {
        this.supplier = supplier;
        return this;
    }

    public Optional<String> getFilename() {
        return Optional.ofNullable(filename);
    }

    public Package setFilename(@NullOr String filename) {
        this.filename = filename;
        return this;
    }

    public Optional<URI> getSourceLocation() {
        return Optional.ofNullable(sourceLocation);
    }

    public Package setSourceLocation(@NullOr URI location) {
        this.sourceLocation = location;
        return this;
    }

    public Map<String, String> getHashes() {
        return Collections.unmodifiableMap(hash);
    }

    public Package addHash(String format, String hash) {
        this.hash.put(format.toUpperCase(), hash);
        return this;
    }

    public Optional<URL> getHomePage() {
        return Optional.ofNullable(homePage);
    }

    public Package setHomePage(@NullOr URL homePage) {
        this.homePage = homePage;
        return this;
    }

    public Optional<License> getConcludedLicense() {
        if (concludedLicense != null) {
            return Optional.of(concludedLicense);
        } else if (declaredLicense == null) {
            return Optional.ofNullable(detectedLicense);
        }
        return Optional.of(declaredLicense);
    }

    public Package setConcludedLicense(@NullOr License concludedLicense) {
        this.concludedLicense = concludedLicense;
        return this;
    }

    public Optional<License> getDeclaredLicense() {
        return Optional.ofNullable(declaredLicense);
    }

    public Package setDeclaredLicense(@NullOr License license) {
        this.declaredLicense = license;
        return this;
    }

    public Optional<License> getDetectedLicense() {
        return Optional.ofNullable(detectedLicense);
    }

    public Package setDetectedLicense(@NullOr License license) {
        this.detectedLicense = license;
        return this;
    }

    public Optional<String> getCopyright() {
        return Optional.ofNullable(copyright);
    }

    public Package setCopyright(@NullOr String copyright) {
        this.copyright = copyright;
        return this;
    }

    public Optional<String> getSummary() {
        return Optional.ofNullable(summary);
    }

    public Package setSummary(@NullOr String summary) {
        this.summary = summary;
        return this;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Package setDescription(@NullOr String description) {
        this.description = description;
        return this;
    }

    public Optional<String> getAttribution() {
        return Optional.ofNullable(attribution);
    }

    public Package setAttribution(@NullOr String attribution) {
        this.attribution = attribution;
        return this;
    }

    @Override
    public boolean equals(@NullOr Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Package other = (Package) o;
        return Objects.equals(type, other.type)
                && Objects.equals(namespace, other.namespace)
                && Objects.equals(name, other.name)
                && Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, namespace, name, version);
    }

    @Override
    public String toString() {
        return getPurl().canonicalize();
    }
}
