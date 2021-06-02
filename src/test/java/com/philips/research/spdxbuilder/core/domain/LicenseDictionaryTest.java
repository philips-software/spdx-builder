/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LicenseDictionaryTest {
    private final LicenseDictionary dictionary = new LicenseDictionary();

    @Test
    void createsSingleton() {
        final var first = LicenseDictionary.getInstance();
        final var second = LicenseDictionary.getInstance();

        assertThat(first).isInstanceOf(LicenseDictionary.class);
        assertThat(second).isSameAs(first);
    }

    @Test
    void ignoresEmptyLicense() {
        final var license = dictionary.licenseFor(" \t");

        assertThat(license).isEqualTo(License.NONE);
    }

    @Test
    void acceptsVoidLicenses() {
        final var license = dictionary.licenseFor("noassertion");

        assertThat(license.isDefined()).isFalse();
        assertThat(dictionary.getCustomLicenses()).isEmpty();
    }

    @Test
    void normalizesSpdxLicenses() {
        final var license = dictionary.licenseFor(" mit ");

        assertThat(license).isEqualTo(License.of("MIT"));
    }

    @Test
    void createsCustomLicenseIdentifier() {
        final var custom1 = dictionary.licenseFor("First");
        final var custom2 = dictionary.licenseFor("Second");

        assertThat(custom1).isEqualTo(License.of("LicenseRef-1"));
        assertThat(custom2).isEqualTo(License.of("LicenseRef-2"));
    }

    @Test
    void clearsDictionaryForTestingPurposes() {
        dictionary.licenseFor("First");
        dictionary.clear();
        dictionary.licenseFor("Second");

        assertThat(dictionary.getCustomLicenses()).containsOnlyKeys("LicenseRef-1");
    }

    @Test
    void upgradesDeprecatedSpdxLicenses() {
        final var deprecated = dictionary.licenseFor("GPL-2.0-with-classpath-exception");

        assertThat(deprecated).isEqualTo(License.of("GPL-2.0-only").with("Classpath-exception-2.0"));
    }

    @Test
    void reusesCustomLicenseIdentifier() {
        final var custom1 = dictionary.licenseFor("Custom");
        final var custom2 = dictionary.licenseFor("CUSTOM");

        assertThat(custom1).isEqualTo(custom2);
    }

    @Test
    void listsCustomLicenses() {
        dictionary.licenseFor(" First ");
        dictionary.licenseFor("Second");

        assertThat(dictionary.getCustomLicenses()).isEqualTo(Map.of("LicenseRef-1", "First", "LicenseRef-2", "Second"));
    }

    @Test
    void ignoresEmptyException() {
        final var base = License.of("MIT");

        final var license = dictionary.withException(base, " \t");

        assertThat(license).isSameAs(base);
    }

    @Test
    void extendsLicenseWithSpdxException() {
        final var base = License.of("GPL-3.0-only");

        final var license = dictionary.withException(base, " Classpath-exception-2.0 ");

        assertThat(license).isEqualTo(License.of("GPL-3.0-only").with("Classpath-exception-2.0"));
    }

    @Test
    void customLicenseForCustomException() {
        final var base = License.of("MIT");

        final var license = dictionary.withException(base, " Not an exception ");

        assertThat(license).isEqualTo(License.of("LicenseRef-1"));
        assertThat(dictionary.getCustomLicenses()).containsEntry("LicenseRef-1", "MIT WITH Not an exception");
    }

    @Test
    void expandsCustomLicenseElements() {
        final var license = License.of("MIT")
                .and(dictionary.licenseFor("First"))
                .or(dictionary.licenseFor("Second"));

        final var string = dictionary.expand(license);

        assertThat(string).isEqualTo(License.of("MIT").and(License.of("First")).or(License.of("Second")).toString());
    }

    @Test
    void expandsUnknownCustomElements() {
        final var license = License.of("MIT")
                .and(License.of("LicenseRef-abc123"))
                .and(License.of("Unknown"));

        final var string = dictionary.expand(license);

        assertThat(string).isEqualTo(License.of("MIT").and(License.of("?")).and(License.of("Unknown")).toString());
    }
}
