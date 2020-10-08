/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.spdx;

class SpdxLicense {
    static final String VERSION = "3.8";

    //TODO Only allow valid identifiers
    private final String identifier;

    private SpdxLicense(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return SPDX license for the provided textual description
     */
    static SpdxLicense of(String text) {
        // TODO Parse into structure of classes
        return new SpdxLicense(text);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
