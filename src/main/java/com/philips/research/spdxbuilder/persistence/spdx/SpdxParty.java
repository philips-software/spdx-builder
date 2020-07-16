/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Party;

import java.util.Objects;
import java.util.Optional;

public final class SpdxParty {
    private final String type;
    private final String name;
    private final String email;

    private SpdxParty(String type, String name) {
        this(type, name, null);
    }

    private SpdxParty(String type, String name, String email) {
        this.type = type;
        this.name = name;
        this.email = email;
    }

    static SpdxParty tool(String name, String version) {
        if (version != null) {
            name += '-' + version;
        }
        return new SpdxParty("Tool", name);
    }

    static SpdxParty organization(String name) {
        return new SpdxParty("Organization", name);
    }

    static SpdxParty person(String name, String email) {
        return new SpdxParty("Person", name, email);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static SpdxParty from(Optional<Party> party) {
        return party.map(SpdxParty::from).orElse(null);
    }

    static SpdxParty from(Party party) {
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
