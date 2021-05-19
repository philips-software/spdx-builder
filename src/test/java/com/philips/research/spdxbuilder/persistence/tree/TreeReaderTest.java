/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TreeReaderTest {
    private final BillOfMaterials bom = new BillOfMaterials();

    @Test
    void readsTreeFromStream() {
        final var stream = stream(
                "ns/main@1",
                "+-> ns/sub@2");

        new TreeReader(stream, "custom", Path.of("src", "test", "resources", "custom_formats.yml").toFile(), List.of())
                .read(bom);

        assertThat(bom.getPackages()).containsExactly(
                new Package("ns", "main", "1"),
                new Package("ns", "sub", "2"));
    }

    @Test
    void switchesFormats() {
        final var stream = stream(
                "ns/main@1",
                "### rust",
                "├── sub v2");

        new TreeReader(stream, "npm", null, List.of()).read(bom);

        assertThat(bom.getPackages()).containsExactly(
                new Package("ns", "main", "1"),
                new Package("", "sub", "2"));
        final var pkg1 = bom.getPackages().get(0);
        final var pkg2 = bom.getPackages().get(1);
        assertThat(bom.getRelations()).containsExactly(
                new Relation(pkg1, pkg2, Relation.Type.DYNAMIC_LINK));
    }

    class InternalPackages {
        @Test
        void defaultsToReleaseOutput() {
            final var stream = stream("ns/main@1");

            new TreeReader(stream, "npm", null, List.of()).read(bom);

            assertThat(bom.getPackages().get(0).isInternal()).isFalse();
        }

        @Test
        void flagsProductReleaseOutput() {
            final var stream = stream("ns/main@1");

            new TreeReader(stream, "npm", null, List.of())
                    .setRelease(true)
                    .read(bom);

            assertThat(bom.getPackages().get(0).isInternal()).isTrue();
        }

        @Test
        void passesInternalGlobPatterns() {
            final var stream = stream("ns/internal@1");

            new TreeReader(stream, "npm", null, List.of("*/int*"))
                    .setRelease(true)
                    .read(bom);

            assertThat(bom.getPackages().get(0).isInternal()).isTrue();
        }
    }

    @Test
    void throws_streamFailure() throws Exception {
        final var stream = mock(InputStream.class);
        when(stream.available()).thenThrow(new IOException("Failing stream"));
        final var reader = new TreeReader(stream, "maven", null, List.of());

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("read the tree data");
    }

    @Test
    void throws_parsingFailure() {
        final var stream = stream("Not a valid package");
        final var reader = new TreeReader(stream, "npm", null, List.of());

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("package format");
    }

    @NotNull
    private InputStream stream(String... lines) {
        return new ByteArrayInputStream(String.join("\n", lines).getBytes());
    }
}
