/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BillOfMaterialsTest {
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "MIT";

    final BillOfMaterials bom = new BillOfMaterials();
    final Package pkg = new Package(NAMESPACE, NAME, VERSION);
    final BillOfMaterials.QueryLicenses mockQuery = mock(BillOfMaterials.QueryLicenses.class);

    @Test
    void updatesProjectLicenses() {
        when(mockQuery.query(NAMESPACE, NAME, VERSION, LOCATION)).thenReturn(List.of(LICENSE));
        pkg.setLocation(LOCATION);
        bom.addProject(pkg);

        bom.updateLicenses(mockQuery);

        assertThat(pkg.getDetectedLicenses()).contains(LICENSE);
    }

    @Test
    void updatesDependencyLicenses() {
        when(mockQuery.query(NAMESPACE, NAME, VERSION, LOCATION)).thenReturn(List.of(LICENSE));
        pkg.setLocation(LOCATION);
        bom.addDependency(pkg);

        bom.updateLicenses(mockQuery);

        assertThat(pkg.getDetectedLicenses()).contains(LICENSE);
    }
}
