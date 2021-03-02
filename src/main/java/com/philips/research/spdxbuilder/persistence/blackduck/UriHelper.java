/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.util.UUID;

abstract class UriHelper {
    static UUID uuidFromUri(URI uri, int fromEnd) {
        final var part = getPart(uri, fromEnd);
        return UUID.fromString(part);
    }

    static long longFromUri(URI uri, int fromEnd) {
        final var part = getPart(uri, fromEnd);
        return toLong(part);
    }

    private static long toLong(String part) {
        try {
            return Long.parseLong(part);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid integer value: " + part);
        }
    }

    private static String getPart(@NullOr URI uri, int fromEnd) {
        if (uri == null) {
            throw new NullPointerException("No URI provided");
        }
        final var parts = uri.getPath().split("/");
        if (fromEnd >= parts.length) {
            throw new IllegalArgumentException("Expected path of '" + uri + "' to have at least " + (fromEnd + 1) + " parts");
        }
        return parts[parts.length - fromEnd - 1];
    }
}
