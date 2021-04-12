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

            assertThat(bom.getPackages()).contains(new Package("custom", "namespace", "name", "version"));
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
                    new Package("cargo", "", "top-internal", "1.2"),
                    new Package("cargo", "", "internal", "1.3"),
                    new Package("cargo", "", "external", "2.0"),
                    new Package("cargo", "", "second", "0.1n"),
                    new Package("cargo", "", "another", "3.0"));
            assertThat(bom.getPackages().get(0).isInternal()).isTrue();
            assertThat(bom.getPackages().get(2).isInternal()).isFalse();
        }

        @Test
        void pipenv() {
            format.configure(parser, "pipenv");

            parse("alembic==1.5.8",
                    "  - Mako [required: Any, installed: 1.1.4]",
                    "    - MarkupSafe [required: >=0.9.2, installed: 1.1.1]",
                    "  - python-dateutil [required: Any, installed: 2.8.1]",
                    "    - six [required: >=1.5, installed: 1.15.0]",
                    "  - python-editor [required: >=0.3, installed: 1.0.4]",
                    "  - SQLAlchemy [required: >=1.3.0, installed: 1.4.2]",
                    "    - greenlet [required: !=0.4.17, installed: 1.0.0]",
                    "apache-libcloud==3.3.1",
                    "  - requests [required: >=2.5.0, installed: 2.25.1]",
                    "    - certifi [required: >=2017.4.17, installed: 2020.12.5]",
                    "  - gast [required: ==0.3.3, installed: 0.3.4]",
                    "barista-python-client==1.3.5",
                    "  - base58 [required: >=2, installed: 2.1.0]");

            assertThat(bom.getPackages()).containsExactly(
                    new Package("pypi", "", "alembic", "1.5.8"),
                    new Package("pypi", "", "mako", "1.1.4"),
                    new Package("pypi", "", "markupsafe", "1.1.1"),
                    new Package("pypi", "", "python-dateutil", "2.8.1"),
                    new Package("pypi", "", "six", "1.15.0"),
                    new Package("pypi", "", "python-editor", "1.0.4"),
                    new Package("pypi", "", "sqlalchemy", "1.4.2"),
                    new Package("pypi", "", "greenlet", "1.0.0"),
                    new Package("pypi", "", "apache-libcloud", "3.3.1"),
                    new Package("pypi", "", "requests", "2.25.1"),
                    new Package("pypi", "", "certifi", "2020.12.5"),
                    new Package("pypi", "", "gast", "0.3.4"),
                    new Package("pypi", "", "barista-python-client", "1.3.5"),
                    new Package("pypi", "", "base58", "2.1.0"));
        }

        private void parse(String... lines) {
            for (var l : lines) {
                parser.parse(l);
            }
        }
    }
}
