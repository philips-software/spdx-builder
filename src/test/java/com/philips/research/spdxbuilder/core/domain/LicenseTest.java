/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseTest {
    private static final String IDENTIFIER = "Identifier";
    private static final String EXCEPTION = "Exception";
    private static final License NO_LICENSE = License.of("");

    @Nested
    class ProgrammaticConstruction {
        @Test
        void createsNoLicenseFromEmptyString() {
            assertThat(NO_LICENSE.toString()).isEqualTo("");
            assertThat(NO_LICENSE.isDefined()).isFalse();
        }

        @Test
        void createsSingleLicense() {
            var license = License.of(IDENTIFIER);

            assertThat(license.toString()).isEqualTo(IDENTIFIER);
            assertThat(license.isDefined()).isTrue();
        }

        @Test
        void addsExceptionToSingleLicense() {
            var license = License.of(IDENTIFIER).with(EXCEPTION);

            assertThat(license.toString()).isEqualTo(IDENTIFIER + " WITH " + EXCEPTION);
        }

        @Test
        void throws_doubleWithLicense() {
            assertThatThrownBy(() -> License.of(IDENTIFIER).with(EXCEPTION).with(EXCEPTION))
                    .isInstanceOf(LicenseException.class)
                    .hasMessageContaining("not allowed");
        }

        @Test
        void throws_withOnNonSingleLicense() {
            assertThatThrownBy(() -> License.of(IDENTIFIER).and(License.of("Other")).with(EXCEPTION))
                    .isInstanceOf(LicenseException.class)
                    .hasMessageContaining("Cannot add WITH");
        }

        @Test
        void combinesLicensesUsingOr() {
            final var one = License.of("A");
            final var two = License.of("B");
            final var three = License.of("C");

            assertThat(one.or(two).toString()).isEqualTo("A OR B");
            assertThat((one.or(two)).or(three).toString()).isEqualTo("A OR B OR C");
            assertThat(one.or(two.or(three)).toString()).isEqualTo("A OR B OR C");
        }

        @Test
        void combinesLicensesUsingAnd() {
            final var one = License.of("A");
            final var two = License.of("B");
            final var three = License.of("C");

            assertThat(one.and(two).toString()).isEqualTo("A AND B");
            assertThat((one.and(two)).and(three).toString()).isEqualTo("A AND B AND C");
            assertThat(one.and(two.and(three)).toString()).isEqualTo("A AND B AND C");
        }

        @Test
        void ignoresDuplicatesInComboLicenses() {
            final var one = License.of("A");
            final var two = License.of("B");

            assertThat(one.and(one)).isEqualTo(one);
            assertThat(one.or(one)).isEqualTo(one);
            assertThat(one.and(two).and(one)).isEqualTo(one.and(two));
        }

        @Test
        void combinesComboLicenses() {
            final var one = License.of("A");
            final var two = License.of("B");
            final var three = License.of("C");

            assertThat((one.or(two)).and(three).toString()).isEqualTo("(A OR B) AND C");
            assertThat(one.or(two.and(three)).toString()).isEqualTo("(B AND C) OR A");
            assertThat((one.and(two)).or(three).toString()).isEqualTo("(A AND B) OR C");
            assertThat(one.and(two.or(three)).toString()).isEqualTo("(B OR C) AND A");
        }

        @Test
        void combinesNoLicensesIntoNoLicense() {
            assertThat(NO_LICENSE.and(License.of("")).isDefined()).isFalse();
        }

        @Test
        void combinesNoLicenseWithDefinedLicense() {
            final var license = License.of(IDENTIFIER);

            assertThat(NO_LICENSE.and(license)).isEqualTo(license);
        }

        @Test
        void combinesLicenseWithNoLicense() {
            final var license = License.of(IDENTIFIER);

            assertThat(license.and(NO_LICENSE)).isEqualTo(license);
            assertThat(license.or(NO_LICENSE)).isEqualTo(license);
        }

        @Test
        void ignoresAddingNoLicenseToCombo() {
            final var combo = License.of("A").and(License.of("B"));

            assertThat(combo.and(NO_LICENSE)).isEqualTo(combo);
        }
    }

    @Nested
    class Equality {
        @Test
        void implementsHash() {
            final var license = License.of(IDENTIFIER);

            assertThat(license.hashCode()).isNotNull();
            assertThat(license.hashCode()).isNotEqualTo(License.of("Other").hashCode());
            assertThat(License.of("A").hashCode()).isEqualTo(License.of("a").hashCode());
        }

        @Test
        void implementsEquals() {
            final var license = License.of(IDENTIFIER);

            assertThat(license).isEqualTo(license);
            assertThat(License.of("A")).isEqualTo(License.of("a"));
            assertThat(license).isNotEqualTo(null);
            assertThat(license).isEqualTo(License.of(IDENTIFIER));
            //noinspection AssertBetweenInconvertibleTypes
            assertThat(license).isNotEqualTo("42");
            assertThat(License.of("A").and(License.of("B"))).isEqualTo(License.of("B").and(License.of("A")));
        }
    }
}


