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

import com.philips.research.spdxbuilder.core.domain.Package;

public class ExternalReference {
    private final String category;
    private final String type;
    private final Object locator;

    private ExternalReference(String category, String type, Object locator) {
        this.category = category;
        this.type = type;
        this.locator = locator.toString();
    }

    public static ExternalReference purl(Package pkg) {
        return new ExternalReference("PACKAGE-MANAGER", "purl", pkg.getPurl());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", category, type, locator);
    }
}
