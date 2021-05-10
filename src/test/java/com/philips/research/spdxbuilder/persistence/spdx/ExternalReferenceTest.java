/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.github.packageurl.PackageURL;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalReferenceTest {
    @Test
    void createsPurlFromPackageURL() throws Exception {
        final var purl = new PackageURL("pkg:npm/name@version");

        final var ref = new ExternalReference(purl);

        assertThat(ref.toString()).isEqualTo("PACKAGE-MANAGER purl " + purl);
    }
}
