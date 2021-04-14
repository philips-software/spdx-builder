/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UriHelperTest {
    private static final UUID UUID = java.util.UUID.randomUUID();
    private static final long LONG = 123456789;

    @Test
    void extractsUuidFromURI() {
        final var uri = URI.create("blah/" + UUID + "/something/else");

        assertThat(UriHelper.uuidFromUri(uri, 2)).isEqualTo(UUID);
    }

    @Test
    void throws_notValidUuid() {
        final var uri = URI.create("notValid");

        assertThatThrownBy(() -> UriHelper.uuidFromUri(uri, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid UUID string");
    }

    @Test
    void extractsLongFromURI() {
        final var uri = URI.create("blah/" + LONG + "/something/else");

        assertThat(UriHelper.longFromUri(uri, 2)).isEqualTo(LONG);
    }

    @Test
    void throws_notValidLongValue() {
        final var uri = URI.create("notValid");

        assertThatThrownBy(() -> UriHelper.longFromUri(uri, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid integer value");
    }

    @Test
    void throws_insufficientPathSize() {
        final var uri = URI.create("short");

        assertThatThrownBy(() -> UriHelper.uuidFromUri(uri, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("to have at least 2 parts");
    }

    @Test
    void throws_nullUri() {
        //noinspection ConstantConditions
        assertThatThrownBy(() -> UriHelper.uuidFromUri(null, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No URI provided");
    }
}
