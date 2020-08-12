/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageTest {
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";

    final Package pkg = new Package(NAMESPACE, NAME, VERSION);

    @Test
    void createsInstance() {
        assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
        assertThat(pkg.getName()).isEqualTo(NAME);
        assertThat(pkg.getVersion()).isEqualTo(VERSION);
        assertThat(pkg.getConcludedLicense()).isEmpty();
    }

    @Nested
    class Licenses {
        @Test
        void concludesLicenseIfNoFileLicenses() {
            pkg.setDeclaredLicense(LICENSE);

            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        }

        @Test
        void concludesUnknownLicenseForMismatch() {
            pkg.setDeclaredLicense(LICENSE);
            pkg.addDetectedLicense("Other");

            assertThat(pkg.getConcludedLicense()).isEmpty();
        }

        @Test
        void overridesConcludedLicense() {
            pkg.setDeclaredLicense("Other");
            pkg.addDetectedLicense("Other");
            pkg.setConcludedLicense(LICENSE);

            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        }
    }
}
