/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.github.packageurl.PackageURL;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageTest {
    private static final String TYPE = "type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final License LICENSE = License.of("MIT");

    final Package pkg = new Package(NAMESPACE, NAME, VERSION);

    @Test
    void createsAnonymousInstance() {
        assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
        assertThat(pkg.getName()).isEqualTo(NAME);
        assertThat(pkg.getFullName()).isEqualTo(NAMESPACE + '/' + NAME);
        assertThat(pkg.getVersion()).isEqualTo(VERSION);
        assertThat(pkg.isInternal()).isFalse();
        assertThat(pkg.getPurl()).isEmpty();
        assertThat(pkg.getConcludedLicense()).isEmpty();
        assertThat(pkg.getDeclaredLicense()).isEmpty();
        assertThat(pkg.getDetectedLicenses()).isEmpty();
    }

    @Test
    void createsInstanceWithoutNamespace() {
        final var pkg = new Package(null, NAME, VERSION);

        assertThat(pkg.getNamespace()).isEmpty();
        assertThat(pkg.getFullName()).isEqualTo(NAME);
    }

    @Test
    void createsInstanceFromPackageUrl() throws Exception {
        final var purl = new PackageURL("pkg:" + TYPE + '/' + NAMESPACE + '/' + NAME + '@' + VERSION);
        final var fromPurl = new Package(purl);

        assertThat(fromPurl.getNamespace()).isEqualTo(NAMESPACE);
        assertThat(fromPurl.getName()).isEqualTo(NAME);
        assertThat(fromPurl.getVersion()).isEqualTo(VERSION);
        assertThat(fromPurl.getPurl()).contains(purl);
    }

    @Test
    void overridesExplicitPackageUrl() throws Exception {
        final var purl = new PackageURL("pkg:type/custom@1.2.3");
        pkg.setPurl(purl);

        assertThat(pkg.getPurl()).contains(purl);
    }

    @Test
    void tracksInternalPackages() {
        pkg.setInternal(true);

        assertThat(pkg.isInternal()).isTrue();
    }

    @Test
    void addsDetectedLicenses() {
        pkg.addDetectedLicense(LICENSE);
        pkg.addDetectedLicense(LICENSE);
        pkg.addDetectedLicense(License.of("Something custom"));
        pkg.addDetectedLicense(License.NONE);

        assertThat(pkg.getDetectedLicenses()).hasSize(2);
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Package.class)
                .withOnlyTheseFields("namespace", "name", "version")
                .verify();
    }
}
