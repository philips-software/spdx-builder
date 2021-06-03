/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TreeWriterTest {
    private static final PackageURL PURL = toPurl("pkg:generic/parent@1.0");

    private final TreeFormatter formatter = spy(new TreeFormatter());
    private final BillOfMaterials bom = new BillOfMaterials();
    private final TreeWriter writer = new TreeWriter(formatter);

    static PackageURL toPurl(String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nested
    class PackageIdentifier {
        @Test
        void writesPackageWithPackageURL() throws Exception {
            bom.addPackage(new Package(PURL));

            writer.process(bom);

            verify(formatter).node(PURL.canonicalize());
        }

        @Test
        void writesAnonymousPackage() {
            bom.addPackage(new Package("namespace", "name", "version"));

            writer.process(bom);

            verify(formatter).node("pkg:generic/namespace/name@version");
        }

        @Test
        void throws_noValidPurlPossible() {
            bom.addPackage(new Package(null, "", ""));

            assertThatThrownBy(() -> writer.process(bom))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("generic package");
        }
    }

    @Nested
    class Relationships {
        private final Package parent = new Package(toPurl("pkg:generic/parent@1.0"));
        private final Package child1 = new Package(toPurl("pkg:generic/child@1.0"));
        private final Package child2 = new Package(toPurl("pkg:generic/child@2.0"));
        private final InOrder ordered = Mockito.inOrder(formatter);

        @Test
        void listsRoots() {
            bom.addPackage(child1);
            bom.addPackage(child2);

            writer.process(bom);

            verify(formatter).node(child1.toString());
            verify(formatter).node(child2.toString());
            verifyNoMoreInteractions(formatter);
        }

        @Test
        void indentsChildren() {
            bom.addPackage(parent);
            bom.addPackage(child1);
            bom.addPackage(child2);
            bom.addRelation(parent, child1, Relation.Type.DEPENDS_ON);
            bom.addRelation(parent, child2, Relation.Type.DEPENDS_ON);

            writer.process(bom);

            ordered.verify(formatter).node(parent.toString());
            ordered.verify(formatter).indent();
            ordered.verify(formatter).node(child1.toString());
            ordered.verify(formatter).node(child2.toString());
            ordered.verify(formatter).unindent();
        }

        @Test
        void cutsRecursions() {
            bom.addPackage(parent);
            bom.addPackage(child1);
            bom.addPackage(child2);
            bom.addRelation(parent, child1, Relation.Type.DEPENDS_ON);
            bom.addRelation(parent, child2, Relation.Type.DEPENDS_ON);
            bom.addRelation(child1, child2, Relation.Type.DEPENDS_ON);
            bom.addRelation(child2, child1, Relation.Type.DEPENDS_ON);

            writer.process(bom);

            ordered.verify(formatter).node(parent.toString());
            ordered.verify(formatter).indent();
            ordered.verify(formatter).node(child1.toString());
            ordered.verify(formatter).indent();
            ordered.verify(formatter).node(child2.toString());
            ordered.verify(formatter).indent();
            ordered.verify(formatter).node(child1 + " (*)");
            ordered.verify(formatter, times(2)).unindent();
            ordered.verify(formatter).node(child2 + " (*)");
            ordered.verify(formatter).unindent();
            ordered.verifyNoMoreInteractions();
        }

        @Test
        void indicatesRelationshipType() {
            assertRelationship(Relation.Type.DESCENDANT_OF, "[derived]");
            assertRelationship(Relation.Type.DYNAMIC_LINK, "[dynamic]");
            assertRelationship(Relation.Type.STATIC_LINK, "[static]");
        }

        private void assertRelationship(Relation.Type type, String indication) {
            bom.addPackage(parent);
            bom.addPackage(child1);
            bom.addRelation(parent, child1, type);

            writer.process(bom);

            verify(formatter).node(contains(child1 + " " + indication));
        }
    }
}
