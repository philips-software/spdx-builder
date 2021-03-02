/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.github.packageurl.PackageURL;
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
    void createsInstance() throws Exception {
        assertThat(pkg.getType()).isEqualTo(TYPE);
        assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
        assertThat(pkg.getName()).isEqualTo(NAME);
        assertThat(pkg.getVersion()).isEqualTo(VERSION);
        assertThat(pkg.getPurl()).isEqualTo(new PackageURL("pkg:" + TYPE + '/' + NAMESPACE + '/' + NAME + '@' + VERSION));
        assertThat(pkg.getConcludedLicense()).isEmpty();
    }

    @Test
    void encodesPackageUrlElements() throws Exception {
        final var encoded = new Package("type", "!#?@space", "@?!#", "#!?#");

        assertThat(encoded.getPurl()).isEqualTo(new PackageURL("pkg:type/%21%23%3F%40space/%40%3F%21%23@%23%21%3F%23"));
    }

    @Test
    void overridesExplicitPackageUrl() throws Exception {
        final var purl = new PackageURL("pkg:type/custom@1.2.3");
        pkg.setPurl(purl);

        assertThat(pkg.getPurl()).isEqualTo(purl);
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
