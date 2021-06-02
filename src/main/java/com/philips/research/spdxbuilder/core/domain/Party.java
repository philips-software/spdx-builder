/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

/**
 * Named person, tool or organization.
 */
public class Party {
    private final Type type;
    private final String name;

    public Party(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public enum Type {NONE, PERSON, ORGANIZATION, TOOL}

}
