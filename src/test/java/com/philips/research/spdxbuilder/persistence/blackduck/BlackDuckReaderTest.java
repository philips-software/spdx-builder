/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.License;
import com.philips.research.spdxbuilder.core.domain.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BlackDuckReaderTest {
    private static final String TOKEN = "Token";
    private static final String PROJECT = "Project";
    private static final String PROJECT_SHORT = "ProjectShort";
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final String VERSION = "Version";
    private static final String VERSION_SHORT = "VersionShort";
    private static final UUID VERSION_ID = UUID.randomUUID();
    private static final String BLACK_DUCK_VERSION = "BlackDuckVersion";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String SUMMARY = "Summary";

    private final BlackDuckProduct project = mock(BlackDuckProduct.class);
    private final BlackDuckProduct projectVersion = mock(BlackDuckProduct.class);
    private final BlackDuckClient client = mock(BlackDuckClient.class);
    private final BomReader reader = new BlackDuckReader(client, TOKEN, PROJECT_SHORT, VERSION_SHORT);
    private final BillOfMaterials bom = new BillOfMaterials();

    private static PackageURL purlFrom(String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @BeforeEach
    void beforeEach() {
        when(project.getId()).thenReturn(PROJECT_ID);
        when(project.getName()).thenReturn("Project title");
        when(projectVersion.getId()).thenReturn(VERSION_ID);
        when(projectVersion.getName()).thenReturn("Project version");
    }

    @Nested
    class FindProjectVersion {
        @Test
        void authenticatesWithServer() {
            when(client.findProject(PROJECT_SHORT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION_SHORT)).thenReturn(Optional.of(projectVersion));

            reader.read(bom);

            verify(client).authenticate(TOKEN);
        }

        @Test
        void throws_projectNotFound() {
            when(client.findProject(PROJECT_SHORT)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reader.read(bom))
                    .isInstanceOf(BlackDuckException.class)
                    .hasMessageContaining(PROJECT_SHORT);
        }

        @Test
        void throws_projectVersionNotFound() {
            when(client.findProject(PROJECT_SHORT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION_SHORT)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reader.read(bom))
                    .isInstanceOf(BlackDuckException.class)
                    .hasMessageContaining(VERSION_SHORT);
        }
    }

    @Nested
    class ExportBillOfMaterials {
        private static final String DESCRIPTION = "Description";
        private static final String HOMEPAGE = "https://homepage.com";
        private final License LICENSE = License.of("GPL-3.0-only");

        private final BlackDuckComponent component = mockBdComponent(NAME);

        private BlackDuckComponent mockBdComponent(String name) {
            final var component = mock(BlackDuckComponent.class);
            when(component.getId()).thenReturn(UUID.randomUUID());
            when(component.getVersionId()).thenReturn(UUID.randomUUID());
            when(component.getName()).thenReturn(name);
            when(component.getVersion()).thenReturn(VERSION);
            when(component.getLicense()).thenReturn(Optional.of(License.NONE));
            when(component.getPackageUrls()).thenReturn(List.of(purlFrom("pkg:generic/" + NAMESPACE + "/" + name + "@" + VERSION)));
            when(component.getUsages()).thenReturn(List.of("DYNAMICALLY_LINKED"));
            when(client.getComponentDetails(component)).thenReturn(mock(BlackDuckComponentDetails.class));
            return component;
        }

        @BeforeEach
        void beforeEach() {
            when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
            when(client.findProject(PROJECT_SHORT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION_SHORT)).thenReturn(Optional.of(projectVersion));
            when(client.getRootComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(component));
        }

        @Test
        void exportsProjectInformation() {
            when(project.getName()).thenReturn(PROJECT);
            when(projectVersion.getName()).thenReturn(VERSION);

            reader.read(bom);

            assertThat(bom.getTitle()).contains(PROJECT).contains(VERSION);
            assertThat(bom.getComment().orElseThrow()).contains(BLACK_DUCK_VERSION);
        }

        @Test
        void exportsProjectAsRootComponent() {
            when(project.getName()).thenReturn(PROJECT);
            when(projectVersion.getName()).thenReturn(VERSION);
            when(project.getDescription()).thenReturn(Optional.of(DESCRIPTION));
            when(projectVersion.getDescription()).thenReturn(Optional.of(SUMMARY));
            when(projectVersion.getLicense()).thenReturn(Optional.of(LICENSE));
            when(client.getRootComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of());

            reader.read(bom);

            final var root = bom.getPackages().get(0);
            assertThat(root.getName()).isEqualTo(PROJECT);
            assertThat(root.getVersion()).isEqualTo(VERSION);
            assertThat(root.getDescription()).contains(DESCRIPTION);
            assertThat(root.getSummary()).contains(SUMMARY);
            assertThat(root.getConcludedLicense()).contains(LICENSE);
        }

        @Test
        void exportsComponent() throws Exception {
            when(component.getLicense()).thenReturn(Optional.of(LICENSE));
            final var details = mock(BlackDuckComponentDetails.class);
            when(details.getDescription()).thenReturn(Optional.of(DESCRIPTION));
            when(details.getHomepage()).thenReturn(Optional.of(new URL(HOMEPAGE)));
            when(client.getComponentDetails(component)).thenReturn(details);

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(2);
            final var pkg = bom.getPackages().get(1);
            assertThat(pkg.getPurl()).contains(component.getPackageUrls().get(0));
            assertThat(pkg.getName()).isEqualTo(NAME);
            assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(pkg.getVersion()).isEqualTo(VERSION);
            assertThat(pkg.getDeclaredLicense()).isEmpty();
            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
            assertThat(pkg.getDescription()).contains(DESCRIPTION);
            assertThat(pkg.getHomePage()).contains(new URL(HOMEPAGE));
            assertThat(pkg.getSummary()).contains(NAME);
            assertThat(pkg.getSupplier()).isEmpty();
        }

        @Test
        void exportsComponentWithoutOriginAsAnonymous() {
            when(component.getPackageUrls()).thenReturn(List.of());
            when(component.getLicense()).thenReturn(Optional.of(LICENSE));

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(2); // Root + anonymous
            final var pkg = bom.getPackages().get(1);
            assertThat(pkg.getName()).isEqualTo(NAME);
            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
            assertThat(pkg.getPurl()).isEmpty();
            assertThat(bom.getRelations()).containsExactly(
                    new Relation(bom.getPackages().get(0), pkg, Relation.Type.DYNAMICALLY_LINKS)
            );
        }

        @Test
        void exportsComponentWithMultipleOriginsAsTree() {
            final var purl1 = purlFrom("pkg:maven/ns1/purl1@1");
            final var purl2 = purlFrom("pkg:npm/ns2/purl2@2");
            when(component.getPackageUrls()).thenReturn(List.of(purl1, purl2));
            when(component.getLicense()).thenReturn(Optional.of(LICENSE));

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(4); // Root + component + 2x origin
            final var comp = bom.getPackages().get(1);
            assertThat(comp.getName()).isEqualTo(NAME);
            assertThat(comp.getPurl()).isEmpty();
            assertThat(comp.getConcludedLicense()).contains(LICENSE);
            final var origin1 = bom.getPackages().get(2);
            assertThat(origin1.getNamespace()).isEqualTo("ns1");
            assertThat(origin1.getName()).isEqualTo("purl1");
            assertThat(origin1.getPurl()).contains(purl1);
            assertThat(origin1.getConcludedLicense()).contains(LICENSE);
            final var origin2 = bom.getPackages().get(3);
            assertThat(origin2.getNamespace()).isEqualTo("ns2");
            assertThat(origin2.getName()).isEqualTo("purl2");
            assertThat(origin2.getPurl()).contains(purl2);
            assertThat(origin2.getConcludedLicense()).contains(LICENSE);
            assertThat(bom.getRelations()).containsExactlyInAnyOrder(
                    new Relation(bom.getPackages().get(0), comp, Relation.Type.DYNAMICALLY_LINKS),
                    new Relation(comp, origin1, Relation.Type.DEPENDS_ON),
                    new Relation(comp, origin2, Relation.Type.DEPENDS_ON)
            );
        }

        @Test
        void exportsSubprojects() {
            final var projectId = UUID.randomUUID();
            final var versionId = UUID.randomUUID();
            when(component.isSubproject()).thenReturn(true);
            when(component.getId()).thenReturn(projectId);
            when(component.getVersionId()).thenReturn(versionId);
            when(component.getLicense()).thenReturn(Optional.of(LICENSE));
            final var sub = mockBdComponent("sub-project-component");
            when(client.getRootComponents(projectId, versionId)).thenReturn(List.of(sub));

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(3); // Root + subproject + component in subproject
            final var root = bom.getPackages().get(0);
            final var subproject = bom.getPackages().get(1);
            final var subprojectPkg = bom.getPackages().get(2);
            assertThat(subproject.getName()).contains(NAME);
            assertThat(subproject.getVersion()).contains(VERSION);
            assertThat(subproject.getConcludedLicense()).contains(LICENSE);
            assertThat(bom.getRelations()).contains(
                    new Relation(root, subproject, Relation.Type.DYNAMICALLY_LINKS),
                    new Relation(subproject, subprojectPkg, Relation.Type.DYNAMICALLY_LINKS)
            );
        }

        @Nested
        class PackageRelations {
            private final BlackDuckComponent parent = mockBdComponent("parent");

            @Test
            void exportsDependencyRelationships() {
                when(client.getRootComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(parent));
                when(client.getDependencies(PROJECT_ID, VERSION_ID, parent)).thenReturn(List.of(component));

                reader.read(bom);

                assertThat(bom.getPackages()).hasSize(3);
                final var from = bom.getPackages().get(1);
                final var to = bom.getPackages().get(2);
                assertThat(bom.getRelations()).hasSize(2);
                assertThat(bom.getRelations()).contains(
                        new Relation(from, to, Relation.Type.DYNAMICALLY_LINKS));
            }

            @Test
            void exportsAdditionalRelationshipsOnMultipleInstancesOfTheSameComponent() {
                final var child1 = mockBdComponent("child1");
                final var child2 = mockBdComponent("child2");
                when(client.getRootComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(parent, parent));
                //noinspection unchecked
                when(client.getDependencies(PROJECT_ID, VERSION_ID, parent)).thenReturn(List.of(child1), List.of(child2));

                reader.read(bom);

                assertThat(bom.getPackages()).hasSize(1 + 1 + 2); // project + parent + 2x child
                final var parentPkg = bom.getPackages().get(1);
                final var child1Pkg = bom.getPackages().get(2);
                final var child2Pkg = bom.getPackages().get(3);
                assertThat(bom.getRelations()).contains(
                        new Relation(parentPkg, child1Pkg, Relation.Type.DYNAMICALLY_LINKS),
                        new Relation(parentPkg, child2Pkg, Relation.Type.DYNAMICALLY_LINKS)
                );
            }

            @Test
            void mapsRelationshipTypeFromComponentUsage() {
                assertRelationship(List.of("SOURCE_CODE"), Relation.Type.DESCENDANT_OF);
                assertRelationship(List.of("DYNAMICALLY_LINKED"), Relation.Type.DYNAMICALLY_LINKS);
                assertRelationship(List.of("STATICALLY_LINKED"), Relation.Type.STATICALLY_LINKS);
                assertRelationship(List.of("SEPARATE_WORK"), Relation.Type.CONTAINS);
                assertRelationship(List.of("MERELY_AGGREGATED"), Relation.Type.CONTAINS);
                assertRelationship(List.of("IMPLEMENTATION_OF_STANDARD"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("PREREQUISITE"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("DEV_TOOL_EXCLUDED"), Relation.Type.DEVELOPED_USING);
                assertRelationship(List.of("Unknown value"), Relation.Type.DEPENDS_ON);
            }

            @Test
            void choosesStrictestRelationship() {
                assertRelationship(List.of("DYNAMICALLY_LINKED", "SOURCE_CODE"), Relation.Type.DESCENDANT_OF);
                assertRelationship(List.of("SOURCE_CODE", "DYNAMICALLY_LINKED"), Relation.Type.DESCENDANT_OF);
            }

            void assertRelationship(List<String> usages, Relation.Type relationship) {
                when(component.getUsages()).thenReturn(usages);
                when(client.getRootComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(component));
                final var bom = new BillOfMaterials();

                new BlackDuckReader(client, TOKEN, PROJECT_SHORT, VERSION_SHORT).read(bom);

                assertThat(bom.getRelations()).hasSize(1);
                final var relation = bom.getRelations().stream().findFirst().orElseThrow();
                assertThat(relation.getType()).isEqualTo(relationship);
            }
        }
    }
}
