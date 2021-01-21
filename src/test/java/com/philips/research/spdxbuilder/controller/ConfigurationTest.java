/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationTest {
    @Test
    void readsMinimalConfiguration() {
        final var config = read("---\n" +
                "document:\n" +
                "  title: Title");

        assertThat(config.document).isNotNull();
        assertThat(config.projects).isEmpty();
        assertThat(config.curations).isEmpty();
    }

    @Test
    void throws_malformedConfiguration() {
        assertThatThrownBy(() -> read("Not a valid file"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format error");
    }

    @Test
    void throws_emptyConfiguration() {
        assertThatThrownBy(() -> read("---\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is empty");
    }

    @Test
    void readsDocumentProperties() {
        final var config = read("---\n"
                + "document:\n"
                + "  title: Title\n"
                + "  key: Key\n"
                + "  namespace: http://name/space\n"
                + "  comment: Comment\n"
                + "  organization: Organization\n"
        );

        assertThat(config.document.title).isEqualTo("Title");
        assertThat(config.document.key).isEqualTo("Key");
        assertThat(config.document.namespace).isEqualTo(URI.create("http://name/space"));
        assertThat(config.document.comment).isEqualTo("Comment");
        assertThat(config.document.organization).isEqualTo("Organization");
    }

    @Test
    void readsProjectMappings() {
        final var config = read("---\n"
                + "projects:\n"
                + "- id: ProjectId\n"
                + "  purl: pkg:/group/name\n"
        );

        assertThat(config.projects).hasSize(1);
        final var project = config.projects.get(0);
        assertThat(project.id).isEqualTo("ProjectId");
        assertThat(project.purl).isEqualTo(URI.create("pkg:/group/name"));
    }

    @Test
    void readsCurations() {
        final var config = read("---\n"
                + "curations:\n"
                + "- purl: pkg:/group/name\n"
                + "  source: https://example/source\n"
                + "  license: License\n"
        );

        assertThat(config.curations).hasSize(1);
        final var curation = config.curations.get(0);
        assertThat(curation.purl).isEqualTo(URI.create("pkg:/group/name"));
        assertThat(curation.source).isEqualTo(URI.create("https://example/source"));
        assertThat(curation.license).isEqualTo("License");
    }

    @Test
    void providesSampleFormat() {
        final var sample = Configuration.example();

        assertThat(sample).contains("Document").contains("identifier").contains("License");
    }

    private Configuration read(String string) {
        return Configuration.parse(new ByteArrayInputStream(string.getBytes()));
    }
}
