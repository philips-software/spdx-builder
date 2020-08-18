/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Package;

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
        final var purl = String.format("pkg:%s/%s/%s@%s", pkg.getType(), pkg.getNamespace(), pkg.getName(), pkg.getVersion());

        return new ExternalReference("PACKAGE-MANAGER", "purl", purl);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", category, type, locator);
    }
}
