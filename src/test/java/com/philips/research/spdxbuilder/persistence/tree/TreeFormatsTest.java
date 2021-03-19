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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
                    new Package("maven", "com.group", "artifact", "1.2"),
                    new Package("maven", "com.group", "artifact", "1.2.1"),
                    new Package("maven", "com.group", "upgraded", "2.3.4"));
        }

        @Test
        void maven() {
            format.configure(parser, "maven");

            parse("[INFO] group:ignore:666:compile",
                    "[INFO] --- maven-dependency-plugin:3.1.2:tree ---",
                    "[INFO] com.group:artifact:jar:1.2",
                    "[INFO] +- com.group:contained:jar:1.2.3:compile",
                    "[INFO] +- com.group:skipped:jar:1.2.3:test",
                    "[INFO] |  +- com.group:skipped:jar:1.2.3:test",
                    "[INFO] |  +- com.group:skipped:jar:1.2.3:compile",
                    "[INFO] +- com.group:contained:war:2.0:compile",
                    "[INFO] -----------------------------------",
                    "[INFO] group:ignore:666:compile");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("maven", "com.group", "artifact", "1.2"),
                    new Package("maven", "com.group", "contained", "1.2.3"),
                    new Package("maven", "com.group", "contained", "2.0"));
        }

        @Test
        void npm() {
            format.configure(parser, "npm");

            parse("package@1.2 /path/to/source/code/",
                    "├─┬ sub-package@2.0",
                    "│ ├── sub-package@2.0 deduped",
                    "│ ├── @scope/sub-package@2.1");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("npm", "", "package", "1.2"),
                    new Package("npm", "", "sub-package", "2.0"),
                    new Package("npm", "@scope", "sub-package", "2.1"));
        }

        private void parse(String... lines) {
            for (var l : lines) {
                parser.parse(l);
            }
        }
    }
}
