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
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ProjectJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ProjectVersionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
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

    private final BlackDuckClient client = mock(BlackDuckClient.class);
    private final BomReader reader = new BlackDuckReader(client, TOKEN, PROJECT, VERSION);
    private final BillOfMaterials bom = new BillOfMaterials();

    @Test
    void throws_projectNotFound() {
        when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
        when(client.findProject(PROJECT)).thenReturn(Optional.empty());

        assertThatThrownBy(()->reader.read(bom))
                .isInstanceOf(BlackDuckException.class)
                .hasMessageContaining(PROJECT);
    }

    @Test
    void throws_projectVersionNotFound() {
        final var project = withId( new ProjectJson(), PROJECT_ID);
        when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
        when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
        when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.empty());

        assertThatThrownBy(()->reader.read(bom))
        .isInstanceOf(BlackDuckException.class)
        .hasMessageContaining(VERSION);
    }

    @Nested
    class ProjectVersionExists {
        private final ProjectJson project = withId(new ProjectJson(), PROJECT_ID);
        private final ProjectVersionJson projectVersion = withId(new ProjectVersionJson(), VERSION_ID);

        @BeforeEach
        void beforeEach() {
            when(client.getServerVersion()).thenReturn(BLACK_DUCK_VERSION);
            when(client.findProject(PROJECT)).thenReturn(Optional.of(project));
            when(client.findProjectVersion(PROJECT_ID, VERSION)).thenReturn(Optional.of(projectVersion));
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
    }

    private <E extends BlackDuckApi.EntityJson> E withId(E entity, UUID id) {
        entity._meta = new BlackDuckApi.MetaJson();
        entity._meta.href = URI.create("https://server/" + entity.getClass().getSimpleName() + "/" + id);
        return entity;
    }
}
