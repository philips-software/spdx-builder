/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
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

        new TreeReader(stream, "custom", Path.of("src", "test", "resources", "custom_formats.yml").toFile())
                .read(bom);

        assertThat(bom.getPackages()).containsExactly(
                new Package("custom", "ns", "main", "1"),
                new Package("custom", "ns", "sub", "2"));
    }

    @Test
    void switchesFormats() {
        final var stream = stream(
                "ns/main@1",
                "### rust",
                "├── sub v2");

        new TreeReader(stream, "npm", null).read(bom);

        assertThat(bom.getPackages()).containsExactly(
                new Package("npm", "ns", "main", "1"),
                new Package("cargo", "", "sub", "2"));
        final var pkg1 = bom.getPackages().get(0);
        final var pkg2 = bom.getPackages().get(1);
        assertThat(bom.getRelations()).containsExactly(
                new Relation(pkg1, pkg2, Relation.Type.DYNAMIC_LINK));
    }

    @Test
    void throws_streamFailure() throws Exception {
        final var stream = mock(InputStream.class);
        when(stream.available()).thenThrow(new IOException("Failing stream"));
        final var reader = new TreeReader(stream, "maven", null);

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("read the tree data");
    }

    @Test
    void throws_parsingFailure() {
        final var stream = stream("Not a valid package");
        final var reader = new TreeReader(stream, "npm", null);

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("package identifier");
    }

    @NotNull
    private InputStream stream(String... lines) {
        return new ByteArrayInputStream(String.join("\n", lines).getBytes());
    }
}
