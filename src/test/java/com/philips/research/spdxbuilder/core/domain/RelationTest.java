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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelationTest {
    private static final Package FROM = new Package("From", "NS", "Name", "Version");
    private static final Package TO = new Package("To", "NS", "Name", "Version");

    @Test
    void createsInstance() {
        final var relation = new Relation(FROM, TO, Relation.Type.DYNAMIC_LINK);

        assertThat(relation.getFrom()).isEqualTo(FROM);
        assertThat(relation.getTo()).isEqualTo(TO);
        assertThat(relation.getType()).isEqualTo(Relation.Type.DYNAMIC_LINK);
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Relation.class).verify();
    }
}
