package com.philips.research.ort2spdx.core.bom;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Package {
    private final String name;
    private final String version;
    private final Map<String, String> hash = new HashMap<>();

    private Party originator;
    private Party supplier;
    private URI location;
    private URL homePage;
    private String license;
    private String declaredLicense;
    private String copyright;
    private String summary;
    private String description;

    public Package(String name, String version) {
        this.name = name;
        this.version = version;
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

    public Optional<String> getLicense() {
        return Optional.ofNullable(license);
    }

    public Package setLicense(String license) {
        this.license = license;
        return this;
    }

    public Optional<String> getDeclaredLicense() {
        return Optional.ofNullable(declaredLicense);
    }

    public Package setDeclaredLicense(String license) {
        this.declaredLicense = license;
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
}
