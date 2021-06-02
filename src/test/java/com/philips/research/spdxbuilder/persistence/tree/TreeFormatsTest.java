/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TreeFormatsTest {
    private final TreeFormats format = new TreeFormats();
    private final TreeParser parser = mock(TreeParser.class);

    @Test
    void printsSupportedFormatsWithoutCrashing() {
        format.printFormats();
    }

    @Test
    void configuresFormatByName() {
        format.configure(parser, "maven");

        assertThat(Mockito.mockingDetails(parser).getInvocations().size()).isGreaterThan(0);
    }

    @Test
    void throws_unknownFormat() {
        //noinspection ConstantConditions
        assertThatThrownBy(() -> format.configure(parser, "Unknown"))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("Unknown");
    }

    @Nested
    class Formats {
        private final BillOfMaterials bom = new BillOfMaterials();
        private final TreeParser parser = new TreeParser(bom);

        @Test
        void readsExternalFormats() {
            format.extend(Path.of("src", "test", "resources", "custom_formats.yml").toFile())
                    .configure(parser, "custom");

            parse("namespace/name@version");

            assertThat(bom.getPackages()).contains(new Package("namespace", "name", "version"));
        }

        @Test
        void throws_externalConfigurationUnavailable() {
            assertThatThrownBy(() -> format.extend(new File("not_a_file")))
                    .isInstanceOf(TreeException.class)
                    .hasMessageContaining("not_a_file");
        }

        @Test
        void gradle() {
            format.configure(parser, "gradle");

            parse("group:ignored:666",
                    "runtimeClasspath - Compile classpath for source set 'main'.",
                    "+--- com.group:artifact:1.2",
                    "+--- com.group:artifact:1.2.1 (*)",
                    "+--- com.group:upgraded:1.2.3 -> 2.3.4",
                    "",
                    "group:ignored:666");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("com.group", "artifact", "1.2"),
                    new Package("com.group", "artifact", "1.2.1"),
                    new Package("com.group", "upgraded", "2.3.4"));
        }

        @Test
        void maven() {
            format.configure(parser, "maven");

            parse("[INFO] group:ignore:666:compile",
                    "[INFO] --- maven-dependency-plugin:3.1.2:tree ---",
                    "[INFO] COM.GROUP:ARTIFACT:jar:1.2",
                    "[INFO] +- com.group:contained:jar:1.2.3:compile",
                    "[INFO] +- com.group:skipped:jar:1.2.3:test",
                    "[INFO] |  +- com.group:skipped:jar:1.2.3:test",
                    "[INFO] |  +- com.group:skipped:jar:1.2.3:compile",
                    "[INFO] +- com.group:contained:war:2.0:compile",
                    "[INFO] -----------------------------------",
                    "[INFO] group:ignore:666:compile");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("COM.GROUP", "ARTIFACT", "1.2"),
                    new Package("com.group", "contained", "1.2.3"),
                    new Package("com.group", "contained", "2.0"));
        }

        @Test
        void npm() {
            format.configure(parser, "npm");

            parse("package@1.2 /path/to/source/code/",
                    "├─┬ sub-package@2.0",
                    "│ ├── sub-package@2.0 deduped",
                    "│ ├── @scope/sub-package@2.1");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("", "package", "1.2"),
                    new Package("", "sub-package", "2.0"),
                    new Package("@scope", "sub-package", "2.1"));
        }

        @Test
        void rust() {
            format.configure(parser, "rust");

            parse("top-internal v1.2 (/Users/blah)",
                    "├── internal v1.3 (/Users/blah) (*)",
                    "├── external v2.0",
                    "│   ├── second v0.1n (*)",
                    "",
                    "[ignore me]",
                    "another v3.0");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("", "top-internal", "1.2"),
                    new Package("", "internal", "1.3"),
                    new Package("", "external", "2.0"),
                    new Package("", "second", "0.1n"),
                    new Package("", "another", "3.0"));
            assertThat(bom.getPackages().get(0).isInternal()).isTrue();
            assertThat(bom.getPackages().get(2).isInternal()).isFalse();
        }

        @Test
        void pip() {
            format.configure(parser, "pip");

            parse(
                    "first==1.0.0",
                    "NO-CAPITALS==1.2.3",
                    "converts_underscore==2.0.0");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("", "first", "1.0.0"),
                    new Package("", "no-capitals", "1.2.3"),
                    new Package("", "converts-underscore", "2.0.0"));
        }

        @Test
        void pipenv() {
            format.configure(parser, "pipenv");

            parse("top==1.5.8",
                    "  - Second [required: Any, installed: 1.1.4]",
                    "    - Third [required: >=0.9.2, installed: 1.1.1]",
                    "  - no_underscore [required: Any, installed: 2.8.1]",
                    "    - exact-match [required: ==0.3.3, installed: 0.3.3]");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("", "top", "1.5.8"),
                    new Package("", "second", "1.1.4"),
                    new Package("", "third", "1.1.1"),
                    new Package("", "no-underscore", "2.8.1"),
                    new Package("", "exact-match", "0.3.3"));
        }

        @Test
        void spdxTree() {
            format.configure(parser, "spdx");

            parse("TREE start =====",
                    "pkg:type/top@1.0",
                    "  pkg:custom/ns/name@1.1 (*)",
                    "  pkg:type/code@1.2 [derived]",
                    "  pkg:type/static@1.3 [static] (*)",
                    "  pkg:type/dynamic@1.4 [dynamic]",
                    "pkg:type/%40name@%40version",
                    "TREE end =====");

            final var parent = new Package("", "top", "1.0");
            final var dependency = new Package("ns", "name", "1.1");
            final var descendant = new Package("", "code", "1.2");
            final var staticLink = new Package("", "static", "1.3");
            final var dynamicLink = new Package("", "dynamic", "1.4");
            final var escaped = new Package("", "@name", "@version");
            assertThat(bom.getPackages().get(1).getPurl().orElseThrow().getType()).isEqualTo("custom");
            assertThat(bom.getPackages()).containsExactly(
                    parent, dependency, descendant, staticLink, dynamicLink, escaped);
            assertThat(bom.getRelations()).containsExactlyInAnyOrder(
                    new Relation(parent, dependency, Relation.Type.DEPENDS_ON),
                    new Relation(parent, descendant, Relation.Type.DESCENDANT_OF),
                    new Relation(parent, staticLink, Relation.Type.STATIC_LINK),
                    new Relation(parent, dynamicLink, Relation.Type.DYNAMIC_LINK));
        }

        private void parse(String... lines) {
            for (var l : lines) {
                parser.parse(l);
            }
        }
    }
}
