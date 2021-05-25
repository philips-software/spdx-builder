/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Optional;

public class PackageIdentifier {
    String externalNamespace;
    String externalId;

    PackageIdentifier(String namespace, String id) {
        this.externalNamespace = namespace;
        this.externalId = id;
    }

    Optional<PackageURL> getPurl() {
        try {
            return Optional.of(PackageURLBuilder.aPackageURL()
                    .withType(type())
                    .withNamespace(namespace())
                    .withName(name())
                    .withVersion(version())
                    .build());
        } catch (MalformedPackageURLException e) {
            System.err.println("Invalid package URL for namespace '" + externalNamespace + "', id '" + externalId + "': " + e);
            return Optional.empty();
        }
    }

    private String type() {
        switch (externalNamespace) {
            case "arch_linux":
                return "arch";
            case "centos":
            case "fedora":
            case "redhat":
            case "opensuse":
                return "rpm";
            case "crates":
                return "cargo";
            case "dart":
                return "pub";
            case "debian":
            case "ubuntu":
                return "deb";
            case "npmjs":
                return "npm";
            case "rubygems":
                return "gem";
            default:
                return externalNamespace;
        }
    }

    private @NullOr String namespace() {
        switch (externalNamespace) {
            case "maven":
                return fromEnd(':', 2);
            case "github":
            case "gitlab":
            case "bitbucket":
                return fromEnd('/', 1);
            case "centos":
            case "fedora":
            case "opensuse":
            case "ubuntu":
                return externalNamespace;
            case "alpine":
            case "arch_linux":
            case "debian":
                return null;
            default:
                return fromEnd('/', 2);
        }
    }

    private @NullOr String name() {
        switch (externalNamespace) {
            case "maven":
                return fromEnd(':', 1);
            case "github":
            case "gitlab":
            case "bitbucket":
                @SuppressWarnings("ConstantConditions")
                final var name = fromStart(':', 0);
                @SuppressWarnings("ConstantConditions")
                final var pos = name.indexOf('/');
                return  (pos < 0) ? name : name.substring(pos+1);
            case "alpine":
            case "arch_linux":
            case "centos":
            case "debian":
            case "fedora":
            case "opensuse":
            case "ubuntu":
                return fromEnd('/', 2);
            default:
                return fromEnd('/', 1);
        }
    }

    private @NullOr String version() {
        switch (externalNamespace) {
            case "maven":
                return fromStart(':', 2);
            case "github":
            case "gitlab":
            case "bitbucket":
                return fromStart(':', 1);
            case "alpine":
            case "arch_linux":
            case "centos":
            case "debian":
            case "fedora":
            case "opensuse":
            case "ubuntu":
                return fromEnd('/', 1);
            default:
                return fromEnd('/', 0);
        }
    }

    private @NullOr String fromStart(char sep, int offset) {
        final var parts = externalId.split(String.valueOf(sep));
        return (offset < parts.length) ? parts[offset] : null;
    }

    private @NullOr String fromEnd(char sep, int offset) {
        final var parts = externalId.split(String.valueOf(sep));
        return (offset < parts.length) ? parts[parts.length - offset - 1] : null;
    }
}
