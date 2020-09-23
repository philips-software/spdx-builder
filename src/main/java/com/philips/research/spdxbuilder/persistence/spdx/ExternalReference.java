/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Package;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        final var type = encode(pkg.getType());
        final var namespace = encode(pkg.getNamespace());
        final var name = encode(pkg.getName());
        final var version = encode(pkg.getVersion());
        final var purl = !namespace.isBlank()
                ? String.format("pkg:%s/%s/%s@%s", type, namespace, name, version)
                : String.format("pkg:%s/%s@%s", type, name, version);

        return new ExternalReference("PACKAGE-MANAGER", "purl", purl);
    }

    private static String encode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", category, type, locator);
    }
}
