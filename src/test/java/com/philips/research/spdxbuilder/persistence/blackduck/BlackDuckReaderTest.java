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
    private static final String TYPE = "maven";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME= "Name";
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
            origin.origin=URI.create(String.format("https://server/components/%s/versions/%s/origins/%s",
                    COMPONENT_ID, COMPONENT_VERSION_ID, ORIGIN_ID));
            origin.externalNamespace = TYPE;
            origin.externalId = String.format("%s:%s:%s", NAMESPACE , NAME, VERSION);
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
            when(client.getComponents(PROJECT_ID, VERSION_ID)).thenReturn(List.of(component));

            reader.read(bom);

            assertThat(bom.getPackages()).hasSize(1);
            final var pkg = bom.getPackages().get(0);
            assertThat(pkg.getType()).isEqualTo(TYPE);
            assertThat(pkg.getName()).isEqualTo(NAME);
            assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(pkg.getVersion()).isEqualTo(VERSION);
            assertThat(pkg.getSummary()).contains(SUMMARY);
        }

        //TODO skipsIgnoredComponents
        //TODO skipsComponentsWithoutOrigin
        //TODO skipsDuplicateOrigins
        //TODO exportsRelationships
    }
}
