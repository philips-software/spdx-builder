/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Single bill-of-materials package.
 */
public class Package {
    private final String type;
    private final String namespace;
    private final String name;
    private final String version;
    private final Map<String, String> hash = new HashMap<>();
    private Party originator;
    private Party supplier;
    private String filename;
    private URI location;
    private URL homePage;
    private String concludedLicense;
    private String declaredLicense;
    private String detectedLicense;
    private String copyright;
    private String summary;
    private String description;
    private String attribution;

    public Package(String type, String namespace, String name, String version) {
        this.type = type;
        this.namespace = namespace;
        this.name = name;
        this.version = version;
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

    public Optional<Party> getOriginator() {
        return Optional.ofNullable(originator);
    }

    public Package setOriginator(Party originator) {
        this.originator = originator;
        return this;
    }

    public Optional<Party> getSupplier() {
        return Optional.ofNullable(supplier);
    }

    public Package setSupplier(Party supplier) {
        this.supplier = supplier;
        return this;
    }

    public Optional<String> getFilename() {
        return Optional.ofNullable(filename);
    }

    public Package setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Optional<URI> getLocation() {
        return Optional.ofNullable(location);
    }

    public Package setLocation(URI location) {
        this.location = location;
        return this;
    }

    public Optional<String> getHash(String format) {
        return Optional.ofNullable(hash.get(format.toUpperCase()));
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

    public Package setHomePage(URL homePage) {
        this.homePage = homePage;
        return this;
    }

    public Optional<String> getConcludedLicense() {
        if (concludedLicense != null) {
            return Optional.of(concludedLicense);
        }
        if (declaredLicense == null || Objects.equals(detectedLicense, declaredLicense)) {
            return Optional.ofNullable(detectedLicense);
        }
        return Optional.empty();
    }

    public Package setConcludedLicense(String concludedLicense) {
        this.concludedLicense = concludedLicense;
        return this;
    }

    public Optional<String> getDeclaredLicense() {
        return Optional.ofNullable(declaredLicense);
    }

    public Package setDeclaredLicense(String license) {
        this.declaredLicense = license;
        return this;
    }

    public Optional<String> getDetectedLicense() {
        return Optional.ofNullable(detectedLicense);
    }

    public Package setDetectedLicense(String license) {
        this.detectedLicense = license;
        return this;
    }

    public Optional<String> getCopyright() {
        return Optional.ofNullable(copyright);
    }

    public Package setCopyright(String copyright) {
        this.copyright = copyright;
        return this;
    }

    public Optional<String> getSummary() {
        return Optional.ofNullable(summary);
    }

    public Package setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Package setDescription(String description) {
        this.description = description;
        return this;
    }

    public Optional<String> getAttribution() {
        return Optional.ofNullable(attribution);
    }

    public Package setAttribution(String attribution) {
        this.attribution = attribution;
        return this;
    }
}
