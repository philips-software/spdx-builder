/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.ConversionService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TreeConfigurationTest {
    private static final String TITLE = "Title";
    private static final String ORGANIZATION = "Organization";
    private static final String COMMENT = "Comment";
    private static final String KEY = "Key";
    private static final URI NAMESPACE = URI.create("https://example.com/namespace");

    private final ConversionService service = mock(ConversionService.class);

    @Test
    void parsesConfiguration() {
        final var config = read("document:",
                "  Title: " + TITLE,
                "  Organization: " + ORGANIZATION,
                "  comment: " + COMMENT,
                "  key: " + KEY,
                "  NAMESPACE: " + NAMESPACE);

        config.apply(service);

        verify(service).setDocument(TITLE, ORGANIZATION);
        verify(service).setComment(COMMENT);
        verify(service).setDocReference(KEY);
        verify(service).setDocNamespace(NAMESPACE);
    }

    @Test
    void parsesEmptyConfigurationWithoutException() {
        read("document:").apply(service);

        verify(service).setDocument("", "");
    }

    @Test
    void throws_malformedConfiguration() {
        assertThatThrownBy(() -> read("NoValidSection"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format error");
    }

    private TreeConfiguration read(String... lines) {
        final var file = String.join("\n", lines);
        return TreeConfiguration.parse(new ByteArrayInputStream(file.getBytes()));
    }
}
