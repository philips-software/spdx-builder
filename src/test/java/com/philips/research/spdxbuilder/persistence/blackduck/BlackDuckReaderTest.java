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

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Relation;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ComponentJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.OriginJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ProjectJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ProjectVersionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
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
    private static final UUID ORIGIN_ID = UUID.randomUUID();
    private static final UUID ORIGIN_ID2 = UUID.randomUUID();
    private static final String TYPE = "maven";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String SUMMARY = "Summary";

    private final BlackDuckClient client = mock(BlackDuckClient.class);
    private final BomReader reader = new BlackDuckReader(client, TOKEN, PROJECT, VERSION);
    private final BillOfMaterials bom = new BillOfMaterials();

    @Test
    void throws_projectNotFound() {
        when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
        when(client.findProject(PROJECT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(BlackDuckException.class)
                .hasMessageContaining(PROJECT);
    }

    @Test
    void throws_projectVersionNotFound() {
        final var project = withId(new ProjectJson(), PROJECT_ID);
        when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
        when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
        when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reader.read(bom))
                .isInstanceOf(BlackDuckException.class)
                .hasMessageContaining(VERSION);
    }

    private <E extends BlackDuckApi.EntityJson> E withId(E entity, UUID id) {
        entity._meta = new BlackDuckApi.MetaJson();
        entity._meta.href = URI.create("https://server/" + entity.getClass().getSimpleName() + "/" + id);
        return entity;
    }

    @Nested
    class ProjectVersionExists {
        private final ProjectJson project = withId(new ProjectJson(), PROJECT_ID);
        private final ProjectVersionJson projectVersion = withId(new ProjectVersionJson(), VERSION_ID);
        private final ComponentJson component = new ComponentJson();
        private final OriginJson origin = new OriginJson();

        @BeforeEach
        void beforeEach() {
            when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
            when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.of(projectVersion));
            when(client.getComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(component));
            origin.origin = URI.create(String.format("https://server/components/%s/versions/%s/origins/%s",
                    COMPONENT_ID, COMPONENT_VERSION_ID, ORIGIN_ID));
            origin.externalNamespace = TYPE;
            origin.externalId = String.format("%s:%s:%s", NAMESPACE, NAME, VERSION);
            component.origins.add(origin);
        }

        @Test
        void readsEmptyProjectVersion() {
            reader.read(bom);

            verify(client).authenticate(TOKEN);
            final var comment = bom.getComment().orElseThrow();
            assertThat(comment).contains(PROJECT);
            assertThat(comment).contains(VERSION);
            assertThat(comment).contains(BLACK_DUCK_VERSION);
        }

        @Test
        void exportsComponent() {
            component.componentName = SUMMARY;

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(1);
            final var pkg = bom.getPackages().get(0);
            assertThat(pkg.getType()).isEqualTo(TYPE);
            assertThat(pkg.getName()).isEqualTo(NAME);
            assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(pkg.getVersion()).isEqualTo(VERSION);
            assertThat(pkg.getSummary()).contains(SUMMARY);
        }

        @Test
        void skipsIgnoredComponents() {
            component.ignored = true;

            reader.read(bom);

            assertThat(bom.getPackages()).isEmpty();
        }

        @Test
        void skipsComponentWithoutOrigin() {
            component.origins.clear();

            reader.read(bom);

            assertThat(bom.getPackages()).isEmpty();
        }

        @Test
        void ignoresDuplicateOrigins() {
            component.origins.add(origin);

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(1);
        }

        @Nested
        class PackageRelations {
            private final OriginJson origin2 = new OriginJson();

            @BeforeEach
            void beforeEach() {
                origin2.externalNamespace = origin.externalNamespace;
                origin2.externalId = origin.externalNamespace;
                origin2.origin = URI.create(String.format("https://server/components/%s/versions/%s/origins/%s",
                        COMPONENT_ID, COMPONENT_VERSION_ID, ORIGIN_ID2));
            }

            @Test
            void exportsRelationships() {
                component.origins.add(origin2);
                final var dependency = withId(new BlackDuckApi.DependencyJson(), ORIGIN_ID2);
                when(client.getDependencies(origin)).thenReturn(List.of(dependency));

                reader.read(bom);

                final var from = bom.getPackages().get(0);
                final var to = bom.getPackages().get(1);
                assertThat(bom.getRelations()).hasSize(1);
                assertThat(bom.getRelations()).containsAnyOf(
                        new Relation(from, to, Relation.Type.DEPENDS_ON),
                        new Relation(to, from, Relation.Type.DEPENDS_ON));
            }

            @Test
            void ignoresRelationshipsOutsideBom() {
                final var dependency = withId(new BlackDuckApi.DependencyJson(), ORIGIN_ID2);
                when(client.getDependencies(origin)).thenReturn(List.of(dependency));

                reader.read(bom);

                assertThat(bom.getRelations()).isEmpty();
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
                final var bom = new BillOfMaterials();
                component.origins.add(origin2);
                component.usages = usages;
                final var dependency = withId(new BlackDuckApi.DependencyJson(), ORIGIN_ID2);
                when(client.getDependencies(origin)).thenReturn(List.of(dependency));

                reader.read(bom);

                assertThat(bom.getRelations()).hasSize(1);
                final var relation = bom.getRelations().stream().findFirst().orElseThrow();
                assertThat(relation.getType()).isEqualTo(relationship);
            }
        }
    }
}
