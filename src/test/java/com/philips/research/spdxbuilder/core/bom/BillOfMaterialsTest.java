/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static com.philips.research.spdxbuilder.core.ConversionStore.LicenseInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BillOfMaterialsTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "MIT";

    final BillOfMaterials bom = new BillOfMaterials();
    final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);
    final Package other = new Package(TYPE, NAMESPACE, "Other", VERSION);
    final BillOfMaterials.QueryLicense mockQuery = mock(BillOfMaterials.QueryLicense.class);

    @Test
    void addsUniqueRelationOnlyOnce() {
        bom.addRelation(pkg, other, Relation.Type.DEPENDS_ON);
        bom.addRelation(pkg, other, Relation.Type.DEPENDS_ON);

        assertThat(bom.getRelations()).hasSize(1);
        assertThat(bom.getRelations()).containsExactly(new Relation(pkg, other, Relation.Type.DEPENDS_ON));
    }

    @Test
    void updatesConcludedLicense_noDeclaredLicense() {
        when(mockQuery.query(NAMESPACE, NAME, VERSION, LOCATION))
                .thenReturn(Optional.of(new LicenseInfo(LICENSE, false)));
        pkg.setLocation(LOCATION);
        bom.addPackage(pkg);

        bom.updateLicense(mockQuery);

        assertThat(pkg.getDetectedLicense()).contains(LICENSE);
        assertThat(pkg.getConcludedLicense()).contains(LICENSE);
    }

    @Test
    void updatesOnlyDetectedLicense_conflictWithDeclaredLicense() {
        when(mockQuery.query(NAMESPACE, NAME, VERSION, LOCATION))
                .thenReturn(Optional.of(new LicenseInfo(LICENSE, false)));
        pkg.setLocation(LOCATION);
        pkg.setDeclaredLicense("Other");
        bom.addPackage(pkg);

        bom.updateLicense(mockQuery);

        assertThat(pkg.getDetectedLicense()).contains(LICENSE);
        assertThat(pkg.getConcludedLicense()).isEmpty();
    }

    @Test
    void updatesConcludedLicense_detectedLicenseConfirmed() {
        when(mockQuery.query(NAMESPACE, NAME, VERSION, LOCATION))
                .thenReturn(Optional.of(new LicenseInfo(LICENSE, true)));
        pkg.setLocation(LOCATION);
        pkg.setDeclaredLicense("Other");
        bom.addPackage(pkg);

        bom.updateLicense(mockQuery);

        assertThat(pkg.getDetectedLicense()).contains(LICENSE);
        assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        assertThat(pkg.getConcludedLicense()).isNotEqualTo(pkg.getDeclaredLicense());
    }
}
