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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlackDuckApiTest {
    @Test
    void convertsOriginToPackageURL() {
        assertPurl("maven", "Group:Name:Version", "maven", "Group", "Name", "Version");
        assertPurl("maven", ":Name:Version", "maven", "", "Name", "Version");
        assertPurl("UNKNOWN", "Name/Version", "generic", "", "Name", "Version");
        assertPurl("npmjs", "Name/Version", "npm", "", "Name", "Version");
    }

    private void assertPurl(String externalNamespace, String externalId, String type, String namespace, String name, String version) {
        final var origin = new BlackDuckApi.OriginJson();
        origin.externalNamespace = externalNamespace;
        origin.externalId = externalId;
        assertThat(origin.getType()).isEqualTo(type);
        assertThat(origin.getNamespace()).isEqualTo(namespace);
        assertThat(origin.getName()).isEqualTo(name);
        assertThat(origin.getVersion()).isEqualTo(version);
    }
}
