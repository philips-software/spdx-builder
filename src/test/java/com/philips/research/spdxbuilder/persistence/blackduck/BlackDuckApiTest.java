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

import com.github.packageurl.PackageURL;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlackDuckApiTest {
    @Test
    void convertsOriginToPackageURL() throws Exception {
        assertPurl("maven", "Group:Name:Version", new PackageURL("pkg:maven/Group/Name@Version"));
        assertPurl("maven", "::Name:Version", new PackageURL("pkg:maven/Name@Version"));
        assertPurl("maven", "Name:Version", new PackageURL("pkg:maven/Name@Version"));
        assertPurl("UNKNOWN", "Name/Version", new PackageURL("pkg:generic/Name@Version"));
        assertPurl("npmjs", "Name/Version", new PackageURL("pkg:npm/Name@Version"));
    }

    private void assertPurl(String externalNamespace, String externalId, PackageURL purl) {
        final var origin = new BlackDuckApi.OriginJson();
        origin.externalNamespace = externalNamespace;
        origin.externalId = externalId;
        assertThat(origin.getPurl()).isEqualTo(purl);
    }
}
