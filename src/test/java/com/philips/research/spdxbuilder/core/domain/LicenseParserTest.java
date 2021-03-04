/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LicenseParserTest {
    private static final String IDENTIFIER = "MIT";
    private static final String IDENTIFIER2 = "Apache-2.0";
    private static final String IDENTIFIER3 = "GPL-2.0-only";
    private static final String EXCEPTION = "SHL-2.0";

    private final LicenseDictionary dictionary = LicenseDictionary.getInstance();

    @BeforeEach()
    void beforeEach() {
        dictionary.clear();
    }

    @Test
    void parsesNoLicense() {
        assertThat(LicenseParser.parse("")).isEqualTo(License.NONE);
        assertThat(LicenseParser.parse("   ")).isEqualTo(License.NONE);
        assertThat(LicenseParser.parse("( )")).isEqualTo(License.NONE);
        assertThat(LicenseParser.parse(null)).isEqualTo(License.NONE);
    }

    @Test
    void parsesSingleLicense() {
        var license = LicenseParser.parse(IDENTIFIER);

        assertThat(license).isEqualTo(License.of(IDENTIFIER));
    }

    @Test
    void withExceptionClause() {
        var license = LicenseParser.parse(IDENTIFIER + " with " + EXCEPTION);

        assertThat(license).isEqualTo(License.of(IDENTIFIER).with(EXCEPTION));
    }

    @Test
    void parsesSingleBracketedLicense() {
        var license = LicenseParser.parse("(" + IDENTIFIER + ")");

        assertThat(license).isEqualTo(License.of(IDENTIFIER));
    }

    @Test
    void ignoresUnbalancedOpenBracket() {
        final var license = LicenseParser.parse("(" + IDENTIFIER);

        assertThat(license).isEqualTo(License.of(IDENTIFIER));
    }

    @Test
    void ignoresUnbalancedClosingBracket() {
        final var license = LicenseParser.parse(IDENTIFIER + ")");

        assertThat(license).isEqualTo(License.of(IDENTIFIER));
    }

    @Test
    void ignoresRogueWithClause() {
        final var license = LicenseParser.parse("with " + EXCEPTION);

        assertThat(license.toString()).contains("Ref");
        assertThat(dictionary.getCustomLicenses()).containsValue("with " + EXCEPTION);
    }

    @Test
    void throws_doubleWithClause() {
        final var text = IDENTIFIER + " WITH " + EXCEPTION + " with " + EXCEPTION;
        final var license = LicenseParser.parse(text);

        assertThat(license.toString()).contains("Ref");
        assertThat(dictionary.getCustomLicenses()).containsEntry(license.toString(), text);
    }

    @Test
    void parsesOrCombination() {
        var license = LicenseParser.parse(IDENTIFIER + " or " + IDENTIFIER2 + " or " + IDENTIFIER3);

        assertThat(license).isEqualTo(License.of(IDENTIFIER).or(License.of(IDENTIFIER2)).or(License.of(IDENTIFIER3)));
    }

    @Test
    void parsesAndCombination() {
        var license = LicenseParser.parse(IDENTIFIER + " and " + IDENTIFIER2 + " and " + IDENTIFIER3);

        assertThat(license).isEqualTo(License.of(IDENTIFIER).and(License.of(IDENTIFIER2)).and(License.of(IDENTIFIER3)));
    }

    @Test
    void parsesMixedLogicCombination() {
        var license = LicenseParser.parse(IDENTIFIER + " or " + IDENTIFIER2 + " and " + IDENTIFIER3);

        assertThat(license).isEqualTo(License.of(IDENTIFIER).or(License.of(IDENTIFIER2)).and(License.of(IDENTIFIER3)));
    }

    @Test
    void throws_licensesWithoutLogicalOperator() {
        final var license = LicenseParser.parse(IDENTIFIER + " " + IDENTIFIER2);

        assertThat(license.toString()).contains("Ref");
        assertThat(dictionary.getCustomLicenses()).containsEntry(license.toString(), IDENTIFIER + " " + IDENTIFIER2);
    }

    @Test
    void addsWithClauseToLatestParsedLicense() {
        var license = LicenseParser.parse(IDENTIFIER + " or " + IDENTIFIER2 + " with " + EXCEPTION + " and " + IDENTIFIER3);

        assertThat(license).isEqualTo(License.of(IDENTIFIER).or(License.of(IDENTIFIER2).with(EXCEPTION)).and(License.of(IDENTIFIER3)));
    }

    @Test
    void parsesBracketedCombination() {
        var license = LicenseParser.parse(IDENTIFIER + " or (" + IDENTIFIER2 + " and " + IDENTIFIER3 + ")");

        assertThat(license).isEqualTo(License.of(IDENTIFIER).or(License.of(IDENTIFIER2).and(License.of(IDENTIFIER3))));
    }

    @Test
    void parsesNestedBrackets() {
        var license = LicenseParser.parse("((" + IDENTIFIER + ") or (" + IDENTIFIER2 + "))");

        assertThat(license).isEqualTo(License.of(IDENTIFIER).or(License.of(IDENTIFIER2)));
    }

    @Test
    void withClauseFollowedByOpeningBracketIsAndRelationWithoutException() {
        final var license = LicenseParser.parse(IDENTIFIER + " with (" + EXCEPTION + ")");

        assertThat(license.toString()).contains(IDENTIFIER).contains("Ref");
        assertThat(dictionary.getCustomLicenses()).hasSize(1).containsValue(EXCEPTION);
    }
}
