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

import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Objects;

public final class Relation {
    private final Package from;
    private final Package to;
    private final Type type;

    public Relation(Package from, Package to, Type type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public Package getFrom() {
        return from;
    }

    public Package getTo() {
        return to;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(@NullOr Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equals(from, relation.from) &&
                Objects.equals(to, relation.to) &&
                type == relation.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, type);
    }

    public enum Type {DYNAMIC_LINK, STATIC_LINK, DEPENDS_ON, DESCENDANT_OF}
}
