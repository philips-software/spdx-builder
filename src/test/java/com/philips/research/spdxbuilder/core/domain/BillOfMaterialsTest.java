/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BillOfMaterialsTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";

    final BillOfMaterials bom = new BillOfMaterials();
    final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);
    final Package other = new Package(TYPE, NAMESPACE, "Other", VERSION);

    @Test
    void createsInstance() {
        assertThat(bom.getTitle()).isEmpty();
        assertThat(bom.getComment()).isEmpty();
        assertThat(bom.getOrganization()).isEmpty();
        assertThat(bom.getIdentifier()).isEmpty();
        assertThat(bom.getNamespace()).isEmpty();
    }

    @Test
    void blankDocumentReferenceIsIgnored() {
        bom.setIdentifier("  ");

        assertThat(bom.getIdentifier()).isEmpty();
    }

    @Test
    void addsUniqueRelationOnlyOnce() {
        bom.addRelation(pkg, other, Relation.Type.DEPENDS_ON);
        bom.addRelation(pkg, other, Relation.Type.DEPENDS_ON);

        assertThat(bom.getRelations()).hasSize(1);
        assertThat(bom.getRelations()).containsExactly(new Relation(pkg, other, Relation.Type.DEPENDS_ON));
    }

    @Test
    void defaultsTitleToFirstProject() {
        bom.addPackage(pkg);

        assertThat(bom.getTitle()).isEqualTo(NAME);
    }
}
