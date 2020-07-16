package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Party;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpdxPartyTest {
    static private final String NAME = "Name";
    static private final String VERSION = "Version";
    static private final String EMAIL = "e@mail.com";

    @Test
    void createsTool() {
        assertThat(SpdxParty.tool(NAME, VERSION).toString()).isEqualTo("Tool: " + NAME + '-' + VERSION);
        assertThat(SpdxParty.tool(NAME, null).toString()).isEqualTo("Tool: " + NAME);
    }

    @Test
    void createsOrganization() {
        assertThat(SpdxParty.organization(NAME).toString()).isEqualTo("Organization: " + NAME);
    }

    @Test
    void createsPerson() {
        assertThat(SpdxParty.person(NAME, EMAIL).toString()).isEqualTo("Person: " + NAME + " (" + EMAIL + ")");
    }

    @Test
    void createsFromParty() {
        assertThat(SpdxParty.from(new Party(Party.Type.PERSON, NAME))).isEqualTo(SpdxParty.person(NAME, null));
        assertThat(SpdxParty.from(new Party(Party.Type.ORGANIZATION, NAME))).isEqualTo(SpdxParty.organization(NAME));
        assertThat(SpdxParty.from(new Party(Party.Type.TOOL, NAME))).isEqualTo(SpdxParty.tool(NAME, null));
        assertThat(SpdxParty.from(new Party(Party.Type.NONE, NAME))).isNull();
    }

    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(SpdxParty.class).verify();
    }
}
