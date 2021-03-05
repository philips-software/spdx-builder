/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
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
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final String VERSION = "Version";
    private static final UUID VERSION_ID = UUID.randomUUID();
    private static final String BLACK_DUCK_VERSION = "BlackDuckVersion";
    private static final UUID COMPONENT_ID = UUID.randomUUID();
    private static final UUID COMPONENT_VERSION_ID = UUID.randomUUID();
    private static final String TYPE = "maven";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String SUMMARY = "Summary";
    private static final PackageURL PACKAGE_URL = purlFrom(String.format("pkg:%s/%s/%s@%s", TYPE, NAMESPACE, NAME, VERSION));

    private final BlackDuckProduct project = mock(BlackDuckProduct.class);
    private final BlackDuckProduct projectVersion = mock(BlackDuckProduct.class);
    private final BlackDuckClient client = mock(BlackDuckClient.class);
    private final BomReader reader = new BlackDuckReader(client, TOKEN, PROJECT, VERSION);
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
        when(projectVersion.getId()).thenReturn(VERSION_ID);
    }

    @Nested
    class FindProjectVersion {
        @Test
        void authenticatesWithServer() {
            when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.of(projectVersion));

            reader.read(bom);

            verify(client).authenticate(TOKEN);
        }

        @Test
        void throws_projectNotFound() {
            when(client.findProject(PROJECT)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reader.read(bom))
                    .isInstanceOf(BlackDuckException.class)
                    .hasMessageContaining(PROJECT);
        }

        @Test
        void throws_projectVersionNotFound() {
            when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reader.read(bom))
                    .isInstanceOf(BlackDuckException.class)
                    .hasMessageContaining(VERSION);
        }

    }

    @Nested
    class ExportBillOfMaterials {
        private static final String DESCRIPTION = "Description";
        private static final String HOMEPAGE = "https://homepage.com";
        private final License LICENSE = License.of("GPL-3.0-only");

        private final BlackDuckComponent component = mock(BlackDuckComponent.class);
        private final BlackDuckComponentDetails details = mock(BlackDuckComponentDetails.class);

        @BeforeEach
        void beforeEach() throws Exception {
            when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
            when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.of(projectVersion));
            when(client.getComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(component));
            when(client.getComponentDetails(component)).thenReturn(details);
            when(component.getId()).thenReturn(COMPONENT_ID);
            when(component.getVersionId()).thenReturn(COMPONENT_VERSION_ID);
            when(component.getPackageUrls()).thenReturn(List.of(PACKAGE_URL));
            when(component.getLicense()).thenReturn(LICENSE);
            when(details.getDescription()).thenReturn(Optional.of(DESCRIPTION));
            when(details.getHomepage()).thenReturn(Optional.of(new URL(HOMEPAGE)));
        }

        @Test
        void exportsProjectInformation() {
            when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.of(projectVersion));
            when(project.getName()).thenReturn(PROJECT);
            when(projectVersion.getName()).thenReturn(VERSION);

            reader.read(bom);

            assertThat(bom.getTitle()).contains(PROJECT).contains(VERSION);
            assertThat(bom.getComment().orElseThrow()).contains(BLACK_DUCK_VERSION);
        }

        @Test
        void exportsComponent() throws Exception {
            when(component.getName()).thenReturn(SUMMARY);

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(1);
            final var pkg = bom.getPackages().get(0);
            assertThat(pkg.getType()).isEqualTo(TYPE);
            assertThat(pkg.getName()).isEqualTo(NAME);
            assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(pkg.getVersion()).isEqualTo(VERSION);
            assertThat(pkg.getDeclaredLicense()).contains(LICENSE.toString());
            assertThat(pkg.getDescription()).contains(DESCRIPTION);
            assertThat(pkg.getHomePage()).contains(new URL(HOMEPAGE));
            assertThat(pkg.getSummary()).contains(SUMMARY);
        }

        @Test
        void skipsComponentWithoutOrigin() {
            when(component.getPackageUrls()).thenReturn(List.of());

            reader.read(bom);

            assertThat(bom.getPackages()).isEmpty();
        }

        @Nested
        class PackageRelations {
            private final UUID PARENT_ID = UUID.randomUUID();
            private final UUID PARENT_VERSION_ID = UUID.randomUUID();
            private final PackageURL PARENT_PURL = purlFrom("pkg:maven/parent@version");

            private final BlackDuckComponent parent = mock(BlackDuckComponent.class);

            @BeforeEach
            void beforeEach() {
                when(parent.getId()).thenReturn(PARENT_ID);
                when(parent.getVersionId()).thenReturn(PARENT_VERSION_ID);
                when(parent.getPackageUrls()).thenReturn(List.of(PARENT_PURL));
                when(parent.getLicense()).thenReturn(License.NONE);
                when(client.getComponentDetails(parent)).thenReturn(details);
            }

            @Test
            void exportsRelationships() {
                when(client.getComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(parent));
                when(client.getDependencies(PROJECT_ID, VERSION_ID, parent)).thenReturn(List.of(component));

                reader.read(bom);

                assertThat(bom.getPackages()).hasSize(2);
                final var from = bom.getPackages().get(0);
                final var to = bom.getPackages().get(1);
                assertThat(bom.getRelations()).hasSize(1);
                assertThat(bom.getRelations()).containsAnyOf(
                        new Relation(from, to, Relation.Type.DEPENDS_ON),
                        new Relation(to, from, Relation.Type.DEPENDS_ON));
            }

            @Test
            void mapsRelationshipTypeFromComponentUsage() {
                assertRelationship(List.of("SOURCE_CODE"), Relation.Type.DESCENDANT_OF);
                assertRelationship(List.of("DYNAMICALLY_LINKED"), Relation.Type.DYNAMIC_LINK);
                assertRelationship(List.of("STATICALLY_LINKED"), Relation.Type.STATIC_LINK);
                assertRelationship(List.of("SEPARATE_WORK"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("MERELY_AGGREGATED"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("IMPLEMENTATION_OF_STANDARD"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("PREREQUISITE"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("DEV_TOOL_EXCLUDED"), Relation.Type.DEPENDS_ON);
                assertRelationship(List.of("Unknown value"), Relation.Type.DEPENDS_ON);
            }

            @Test
            void choosesStrictestRelationship() {
                assertRelationship(List.of("DYNAMICALLY_LINKED", "SOURCE_CODE"), Relation.Type.DESCENDANT_OF);
                assertRelationship(List.of("SOURCE_CODE", "DYNAMICALLY_LINKED"), Relation.Type.DESCENDANT_OF);
            }

            void assertRelationship(List<String> usages, Relation.Type relationship) {
                when(component.getUsages()).thenReturn(usages);
                when(client.getComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(parent, component));
                when(client.getDependencies(PROJECT_ID, VERSION_ID, parent)).thenReturn(List.of(component));
                final var bom = new BillOfMaterials();

                new BlackDuckReader(client, TOKEN, PROJECT, VERSION).read(bom);

                assertThat(bom.getRelations()).hasSize(1);
                final var relation = bom.getRelations().stream().findFirst().orElseThrow();
                assertThat(relation.getType()).isEqualTo(relationship);
            }
        }
    }
}
