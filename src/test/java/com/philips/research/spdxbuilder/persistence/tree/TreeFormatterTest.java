/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TreeFormatterTest {
    private static final String ONE = "One";
    private static final String TWO = "Two";
    private static final String THREE = "Three";
    private static final String INDENT = "  ";

    private final TreeFormatter formatter = new TreeFormatter();

    @Test
    void listsTopLevel() {
        assertThat(formatter.node(ONE)).isEqualTo(ONE);
        assertThat(formatter.node(TWO)).isEqualTo(TWO);
        assertThat(formatter.node(THREE)).isEqualTo(THREE);
    }

    @Test
    void indentsChildren() {
        assertThat(formatter.node(ONE)).isEqualTo(ONE);
        formatter.indent();
        assertThat(formatter.node(TWO)).isEqualTo(INDENT + TWO);
        assertThat(formatter.node(THREE)).isEqualTo(INDENT + THREE);
    }

    @Test
    void unindentsBackToParent() {
        assertThat(formatter.node(ONE)).isEqualTo(ONE);
        formatter.indent();
        assertThat(formatter.node(TWO)).isEqualTo(INDENT + TWO);
        formatter.unindent();
        assertThat(formatter.node(THREE)).isEqualTo(THREE);
    }
}
