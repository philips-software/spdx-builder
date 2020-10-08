/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core.bom;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";

    final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);

    @Test
    void createsInstance() {
        assertThat(pkg.getType()).isEqualTo(TYPE);
        assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
        assertThat(pkg.getName()).isEqualTo(NAME);
        assertThat(pkg.getVersion()).isEqualTo(VERSION);
        assertThat(pkg.getConcludedLicense()).isEmpty();
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Package.class)
                .withOnlyTheseFields("type", "namespace", "name", "version")
                .verify();
    }

    @Nested
    class Licenses {
        @Test
        void licensesAreOptional() {
            assertThat(pkg.getDetectedLicense()).isEmpty();
            assertThat(pkg.getDeclaredLicense()).isEmpty();
            assertThat(pkg.getConcludedLicense()).isEmpty();
        }

        @Test
        void concludedLicenseDefaultsToDeclaredLicense() {
            pkg.setDeclaredLicense(LICENSE);

            assertThat(pkg.getDeclaredLicense()).contains(LICENSE);
            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        }

        @Test
        void concludedLicenseDefaultsToConcluded_noDeclaredLicense() {
            pkg.setDetectedLicense(LICENSE);

            assertThat(pkg.getDetectedLicense()).contains(LICENSE);
            assertThat(pkg.getDeclaredLicense()).isEmpty();
            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        }

        @Test
        void concludedLicenseOverridesDeclaredLicense() {
            pkg.setDeclaredLicense("Other");
            pkg.setConcludedLicense(LICENSE);

            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        }
    }
}
