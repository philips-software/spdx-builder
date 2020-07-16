package com.philips.research.spdxbuilder.persistence.spdx;

public class SpdxLicense {
    public static final String VERSION = "3.8";

    //TODO Only allow valid identifiers
    private final String identifier;

    private SpdxLicense(String identifier) {
        this.identifier = identifier;
    }

    public static SpdxLicense of(String text) {
        // TODO Parse into structure of classes
        return new SpdxLicense(text);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
