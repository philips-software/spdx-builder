/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Party;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.Objects;
import java.util.Optional;

/**
 * Named organization or individual.
 */
final class SpdxParty {
    private final String type;
    private final String name;
    private final @NullOr String email;

    private SpdxParty(String type, String name) {
        this(type, name, null);
    }

    private SpdxParty(String type, String name, @NullOr String email) {
        this.type = type;
        this.name = name;
        this.email = email;
    }

    /**
     * @return tool description
     */
    static SpdxParty tool(String name, @NullOr String version) {
        if (version != null) {
            name += '-' + version;
        }
        return new SpdxParty("Tool", name);
    }

    /**
     * @return organization description
     */
    static SpdxParty organization(String name) {
        return new SpdxParty("Organization", name);
    }

    /**
     * @return individual description
     */
    static SpdxParty person(String name, @NullOr String email) {
        return new SpdxParty("Person", name, email);
    }

    /**
     * @return description from an optional party entity or null if none was provided
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static @NullOr SpdxParty from(Optional<Party> party) {
        return party.map(SpdxParty::from).orElse(null);
    }

    /**
     * @return description from a party entity
     */
    static @NullOr SpdxParty from(Party party) {
        switch (party.getType()) {
            case PERSON:
                return person(party.getName(), null);
            case TOOL:
                return tool(party.getName(), null);
            case ORGANIZATION:
                return organization(party.getName());
            default:
            case NONE:
                return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpdxParty)) return false;
        SpdxParty spdxParty = (SpdxParty) o;
        return Objects.equals(type, spdxParty.type) &&
                Objects.equals(name, spdxParty.name) &&
                Objects.equals(email, spdxParty.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, email);
    }

    @Override
    public String toString() {
        var string = type + ": " + name;
        if (email != null) {
            string += " (" + email + ')';
        }
        return string;
    }
}
