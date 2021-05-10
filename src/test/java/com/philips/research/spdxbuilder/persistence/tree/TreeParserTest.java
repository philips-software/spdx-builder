/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreeParserTest {
    private static final String TYPE = "type";
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String PACKAGE1 = "namespace:name:1";
    private static final String PACKAGE2 = "namespace:name:2";
    private static final String PACKAGE3 = "namespace:name:3";

    private final BillOfMaterials bom = new BillOfMaterials();
    private final TreeParser parser = new TreeParser(bom);

    @Test
    void throws_missingPackageTypeConfiguration() {
        assertThatThrownBy(() -> parser.parse(PACKAGE1))
                .hasMessageContaining("type=,");
    }

    @Nested
    class PackageDetection {
        @BeforeEach
        void beforeEach() {
            parser.withTypes(Map.of("", TYPE));
        }

        @Test
        void parsesDefaultArtefact() {
            parser.parse(String.format("%s:%s:%s", NAMESPACE, NAME, VERSION));

            assertThat(bom.getPackages()).contains(new Package(NAMESPACE, NAME, VERSION));
        }

        @Test
        void throws_incompleteIdentifier() {
            assertThatThrownBy(() -> parser.parse("incomplete"))
                    .isInstanceOf(TreeException.class)
                    .hasMessageContaining("package identifier");
        }

        @Test
        void parsesCustomPackageDefinition() {
            parser.withName("^([^/]*)/", 1)
                    .withVersion("^[^/]*/([^/]*)@", 1)
                    .withNamespace("([^@]*)$", 1);
            parser.parse(String.format("%s/%s@%s", NAME, VERSION, NAMESPACE));

            assertThat(bom.getPackages()).contains(new Package(NAMESPACE, NAME, VERSION));
        }

        @Test
        void parsesDistinctArtefacts() {
            parser.parse(PACKAGE1);
            parser.parse(PACKAGE2);
            parser.parse(PACKAGE2);

            assertThat(bom.getPackages()).hasSize(2);
        }

        @Test
        void cleansUpLine() {
            parser.withCleanup("\\[.*]");

            parser.parse("[ignore:me]" + PACKAGE1);

            assertThat(bom.getPackages()).contains(new Package(NAMESPACE, NAME, "1"));
        }

        @Test
        void skipsLinesWithoutIdentifier() {
            parser.parse("-----------");
            parser.parse("");

            assertThat(bom.getPackages()).isEmpty();
        }

        @Test
        void supportsCustomIdentifier() {
            parser.withIdentifier(PACKAGE1.substring(0, 1));

            parser.parse("abc123" + PACKAGE1);

            assertThat(bom.getPackages()).contains(new Package(NAMESPACE, NAME, "1"));
        }

        @Test
        void derivesPackageType() {
            parser.withTypes(Map.of("", "default", "Other", "other"))
                    .withType("\\[(.+)]", 1);

            parser.parse(PACKAGE1);
            parser.parse(PACKAGE2 + "  [Other]");

            assertThat(bom.getPackages()).contains(
                    new Package(NAMESPACE, NAME, "1"),
                    new Package(NAMESPACE, NAME, "2"));
        }

        @Test
        void throws_unknownPackageType() {
            parser.withType("\\[(.+)]", 1);

            assertThatThrownBy(() -> parser.parse(PACKAGE1 + " [Unknown]"))
                    .isInstanceOf(TreeException.class)
                    .hasMessageContaining("'Unknown'");
        }

        @Test
        void skipsPackageAndSubtree() {
            parser.withSkip("skip$");

            parser.parse(PACKAGE1);
            parser.parse("->" + PACKAGE2 + " skip");
            parser.parse("--->" + PACKAGE2);
            parser.parse("->" + PACKAGE3);

            assertThat(bom.getPackages()).containsExactly(
                    new Package(NAMESPACE, NAME, "1"),
                    new Package(NAMESPACE, NAME, "3"));
        }

        @Test
        void marksInternalPackages() {
            parser.withInternal("internal$");

            parser.parse(PACKAGE1);
            parser.parse(PACKAGE2 + " internal");

            assertThat(bom.getPackages().get(0).isInternal()).isFalse();
            assertThat(bom.getPackages().get(1).isInternal()).isTrue();
        }
    }

    @Nested
    class DependencyDetection {
        @BeforeEach
        void beforeEach() {
            parser.withTypes(Map.of("", TYPE));
        }

        @Test
        void detectsDependency() {
            parser.parse(PACKAGE1);
            parser.parse("-" + PACKAGE2);
            parser.parse("-" + PACKAGE3);

            final var pkg1 = bom.getPackages().get(0);
            final var pkg2 = bom.getPackages().get(1);
            final var pkg3 = bom.getPackages().get(2);
            assertThat(bom.getRelations()).containsExactlyInAnyOrder(
                    new Relation(pkg1, pkg2, Relation.Type.DYNAMIC_LINK),
                    new Relation(pkg1, pkg3, Relation.Type.DYNAMIC_LINK));
        }

        @Test
        void detectsRecursiveDependency() {
            parser.parse("-" + PACKAGE1);
            parser.parse("+-" + PACKAGE2);
            parser.parse(" +----" + PACKAGE3);

            final var pkg1 = bom.getPackages().get(0);
            final var pkg2 = bom.getPackages().get(1);
            final var pkg3 = bom.getPackages().get(2);
            assertThat(bom.getRelations()).containsExactlyInAnyOrder(
                    new Relation(pkg1, pkg2, Relation.Type.DYNAMIC_LINK),
                    new Relation(pkg2, pkg3, Relation.Type.DYNAMIC_LINK));
        }

        @Test
        void movesBackUpTheDependencyTree() {
            parser.parse(PACKAGE1);
            parser.parse("->" + PACKAGE2);
            parser.parse("---->" + PACKAGE3);
            parser.parse("->" + PACKAGE3);
            parser.parse(PACKAGE3);

            final var pkg1 = bom.getPackages().get(0);
            final var pkg3 = bom.getPackages().get(2);
            assertThat(bom.getRelations()).hasSize(3)
                    .contains(new Relation(pkg1, pkg3, Relation.Type.DYNAMIC_LINK));
        }

        @Test
        void derivesRelationship() {
            parser.withRelationships(Map.of("", Relation.Type.STATIC_LINK.name(), "Dep", Relation.Type.DEPENDS_ON.name()))
                    .withRelationship("\\[(.+)]", 1);

            parser.parse(PACKAGE1);
            parser.parse("->" + PACKAGE2);
            parser.parse("->" + PACKAGE3 + "  [Dep]");

            final var pkg1 = bom.getPackages().get(0);
            final var pkg2 = bom.getPackages().get(1);
            final var pkg3 = bom.getPackages().get(2);
            assertThat(bom.getRelations()).contains(
                    new Relation(pkg1, pkg2, Relation.Type.STATIC_LINK),
                    new Relation(pkg1, pkg3, Relation.Type.DEPENDS_ON));
        }

        @Test
        void throws_unknownRelationshipIdentifier() {
            parser.withRelationship("\\[(.+)]", 1);
            parser.parse(PACKAGE1);

            assertThatThrownBy(() -> parser.parse("-->" + PACKAGE2 + " [Unknown]"))
                    .isInstanceOf(TreeException.class)
                    .hasMessageContaining("'Unknown'");
        }
    }

    @Nested
    class SectionDelimiters {
        @BeforeEach
        void beforeEach() {
            parser.withTypes(Map.of("", TYPE));
        }

        @Test
        void startsAfterPattern() {
            parser.withStartSection("Start");

            parser.parse(PACKAGE1);
            parser.parse("*** Start ***");
            parser.parse(PACKAGE2);

            assertThat(bom.getPackages()).hasSize(1)
                    .contains(new Package(NAMESPACE, NAME, "2"));
        }

        @Test
        void endsAfterPattern() {
            parser.withEndSection("End");

            parser.parse(PACKAGE1);
            parser.parse("*** End ***");
            parser.parse(PACKAGE2);

            assertThat(bom.getPackages()).hasSize(1)
                    .contains(new Package(NAMESPACE, NAME, "1"));
        }

        @Test
        void keepsTemporalOrderingForStartAndEnd() {
            parser.withStartSection("Start").withEndSection("End");

            parser.parse(PACKAGE1);
            parser.parse("*** End ***");
            parser.parse("*** Start ***");
            parser.parse(PACKAGE2);
            parser.parse("*** End ***");
            parser.parse("*** Start ***");
            parser.parse(PACKAGE3);

            assertThat(bom.getPackages()).hasSize(1)
                    .contains(new Package(NAMESPACE, NAME, "2"));
        }
    }

    @Nested
    class SwitchingFormats {
        @BeforeEach
        void beforeEach() {
            parser.withTypes(Map.of("", TYPE));
        }

        @Test
        void detectsFormatMarker() {
            assertThat(parser.parse(PACKAGE1)).isEmpty();
            assertThat(parser.parse("### Format")).contains("Format");
            assertThat(parser.parse("######## Format #####")).contains("Format");
        }
    }
}
