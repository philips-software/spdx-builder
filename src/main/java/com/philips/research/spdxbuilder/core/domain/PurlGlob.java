/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.github.packageurl.PackageURL;
import pl.tlinkowski.annotation.basic.NullOr;

public class PurlGlob {
    private static final String ANY = "*";

    private final String type;
    private final String namespace;
    private final String name;
    private final String version;

    public PurlGlob(String pattern) {
        final var sanitized = sanitized(pattern);

        final var versionPos = posOrLength(sanitized, '@');
        version = versionPos < sanitized.length() ? sanitized.substring(versionPos + 1) : ANY;

        final var path = sanitized.substring(0, versionPos).split("/");
        if (path.length == 1 && !path[0].isEmpty()) {
            type = ANY;
            namespace = ANY;
            name = path[0];
        } else if (path.length == 2) {
            namespace = ANY;
            type = path[0];
            name = path[1];
        } else if (path.length == 3) {
            type = path[0];
            namespace = path[1];
            name = path[2];
        } else {
            throw new IllegalArgumentException("Invalid package URL glob: " + pattern);
        }
    }

    private String sanitized(String purl) {
        final var start = purl.startsWith("pkg:") ? 4 : 0;
        return purl.substring(start, posOrLength(purl, '#', '?'));
    }

    private int posOrLength(String string, char... chars) {
        int pos = string.length();
        for (var ch : chars) {
            final var index = string.indexOf(ch);
            if (index >= 0 && index < pos) {
                pos = index;
            }
        }
        return pos;
    }

    public boolean matches(PackageURL purl) {
        return matches(type, purl.getType())
                && matches(namespace, purl.getNamespace())
                && matches(name, purl.getName())
                && matches(version, purl.getVersion());
    }

    private boolean matches(String pattern, @NullOr String string) {
        string = string == null ? "" : string;
        var pos = 0;
        for (var i = 0; i < pattern.length(); i++) {
            if (pattern.substring(i).equals(string.substring(pos))) {
                return true;
            }
            if (pattern.charAt(i) == '*') {
                final var remaining = pattern.substring(i + 1);
                for (var sub = pos; sub <= string.length(); sub++) {
                    if (matches(remaining, string.substring(sub))) {
                        return true;
                    }
                }
                return false;
            }
            if (pos == string.length() || pattern.charAt(i) != string.charAt(pos)) {
                return false;
            }
            pos++;
        }
        return pattern.isEmpty() && string.isEmpty();
    }
}
