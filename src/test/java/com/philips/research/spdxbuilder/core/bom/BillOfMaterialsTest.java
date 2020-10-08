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

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class BillOfMaterialsTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "License";

    final BillOfMaterials bom = new BillOfMaterials();
    final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);
    final Package other = new Package(TYPE, NAMESPACE, "Other", VERSION);

    @Test
    void addsUniqueRelationOnlyOnce() {
        bom.addRelation(pkg, other, Relation.Type.DEPENDS_ON);
        bom.addRelation(pkg, other, Relation.Type.DEPENDS_ON);

        assertThat(bom.getRelations()).hasSize(1);
        assertThat(bom.getRelations()).containsExactly(new Relation(pkg, other, Relation.Type.DEPENDS_ON));
    }
}
