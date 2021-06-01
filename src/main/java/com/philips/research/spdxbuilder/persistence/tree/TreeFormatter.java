/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

public class TreeFormatter {
    private static final String INDENT = "  ";

    private int indent = 0;

    public String node(String name) {
        return INDENT.repeat(indent) + name;
    }

    public void indent() {
        indent++;
    }

    public void unindent() {
        indent--;
    }
}
