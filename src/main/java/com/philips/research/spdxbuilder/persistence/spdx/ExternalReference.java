/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.github.packageurl.PackageURL;

public class ExternalReference {
    private final String category;
    private final String type;
    private final Object locator;

    public ExternalReference(PackageURL purl) {
        this.category = "PACKAGE-MANAGER";
        this.type = "purl";
        this.locator = purl.canonicalize();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", category, type, locator);
    }
}
