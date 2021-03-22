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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TreeReaderTest {
    private final BillOfMaterials bom = new BillOfMaterials();

    @Test
    void readsTreeFromStream() {
        final var stream = stream("[INFO] --- maven-dependency-plugin:3.1.2:tree ---",
                "[INFO] group:artifact:jar:1",
                "[INFO] +-- group:sub:jar:2");

        new TreeReader(stream, "maven").read(bom);

        assertThat(bom.getPackages()).containsExactly(
                new Package("maven", "group", "artifact", "1"),
                new Package("maven", "group", "sub", "2"));
    }

    @Test
    void throws_streamFailure() throws Exception {
        final var stream = mock(InputStream.class);
        when(stream.available()).thenThrow(new IOException("Failing stream"));
        final var reader = new TreeReader(stream, "maven");

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("read the tree data");
    }

    @Test
    void throws_parsingFailure() {
        final var stream = stream("Not a valid package");
        final var reader = new TreeReader(stream, "npm");

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("package identifier");
    }

    @NotNull
    private InputStream stream(String... lines) {
        return new ByteArrayInputStream(String.join("\n", lines).getBytes());
    }
}
