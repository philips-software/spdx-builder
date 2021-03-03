/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseParserTest {
    private static final String IDENTIFIER = "MIT";
    private static final String IDENTIFIER2 = "Apache-2.0";
    private static final String IDENTIFIER3 = "GPL-2.0-only";
    private static final String EXCEPTION = "SHL-2.0";

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
    void throws_rogueWithClause() {
        assertThatThrownBy(() -> LicenseParser.parse("with " + EXCEPTION))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("WITH");
    }

    @Test
    void throws_doubleWithClause() {
        //TODO This should be a custom license instead
        assertThatThrownBy(() -> LicenseParser.parse(IDENTIFIER + " with " + EXCEPTION + " with " + EXCEPTION))
                .isInstanceOf(LicenseException.class);
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
        //TODO Should become a custom license instead
        assertThatThrownBy(() -> LicenseParser.parse("A B"))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("logical operator");
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
    void throws_unbalancedOpenBracket() {
        //TODO Should be custom license
        assertThatThrownBy(() -> LicenseParser.parse("("))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("opening bracket");
    }

    @Test
    void throws_unbalancedClosingBracket() {
        //TODO Should be custom license
        assertThatThrownBy(() -> LicenseParser.parse(")"))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("closing bracket");
    }

    @Test
    void throws_withClauseFollowedByOpeningBracket() {
        //TODO Should be custom license
        assertThatThrownBy(() -> LicenseParser.parse(IDENTIFIER + " with (" + EXCEPTION + ")"))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("not expected");
    }
}
