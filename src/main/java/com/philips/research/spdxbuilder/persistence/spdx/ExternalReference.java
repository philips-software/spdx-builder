/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
        return new ExternalReference("PACKAGE-MANAGER", "purl", pkg.getPurl().canonicalize());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", category, type, locator);
    }
}
