/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Package;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalReferenceTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";

    @Test
    void createsPurlFromPackage() {
        final var pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);

        final var ref = ExternalReference.purl(pkg);

        assertThat(ref.toString()).isEqualTo("PACKAGE-MANAGER purl pkg:" + TYPE + "/" + NAMESPACE + "/" + NAME + "@" + VERSION);
    }

    @Test
    void createsPurlFromPackageWithoutNamespace() {
        final var pkg = new Package(TYPE, "", NAME, VERSION);

        final var ref = ExternalReference.purl(pkg);

        assertThat(ref.toString()).isEqualTo("PACKAGE-MANAGER purl pkg:" + TYPE + "/" + NAME + "@" + VERSION);
    }

    @Test
    void escapesUriReservedCharacters() {
        final var dangerous = " @?#+%/";
        final var escaped = "%20%40%3F%23%2B%25%2F";
        final var pkg = new Package(dangerous, dangerous, dangerous, dangerous);

        final var ref = ExternalReference.purl(pkg);

        assertThat(ref.toString()).isEqualTo("PACKAGE-MANAGER purl pkg:" + escaped + "/" + escaped + "/" + escaped + "@" + escaped);
    }
}
