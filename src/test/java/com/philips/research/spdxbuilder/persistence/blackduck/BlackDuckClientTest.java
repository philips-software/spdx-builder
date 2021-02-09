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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackDuckClientTest {
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final String BEARER_TOKEN = "BearerToken";
    private static final String PROJECT = "Project";
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final String VERSION = "1.2.3";
    private static final UUID VERSION_ID = UUID.randomUUID();
    private static final String NAME = "Name";
    private static final UUID COMPONENT_ID = UUID.randomUUID();
    private static final UUID ORIGIN_ID = UUID.randomUUID();
    private static final int PORT = 1080;

    private final MockWebServer server = new MockWebServer();
    private BlackDuckClient client;

    @BeforeEach
    void setUp() throws IOException {
        server.start(PORT);
        final var url = server.url("/").url();
        client = new BlackDuckClient(url);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void retrievesAuthenticationToken() throws Exception {
        server.enqueue(new MockResponse().setBody(new JSONObject().put("bearerToken", BEARER_TOKEN).toString()));
        server.enqueue(new MockResponse().setBody(new JSONObject().put("version", VERSION).toString()));

        client.authenticate(ACCESS_TOKEN);
        client.getServerVersion();

        final var auth = server.takeRequest();
        assertThat(auth.getMethod()).isEqualTo("POST");
        assertThat(auth.getPath()).isEqualTo("/api/tokens/authenticate");
        assertThat(auth.getHeader("Authorization")).isEqualTo("token " + ACCESS_TOKEN);
        final var request = server.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("bearer " + BEARER_TOKEN);
    }

    @Test
    void throws_authenticationFailure() {
        server.enqueue(new MockResponse().setResponseCode(401));

        assertThatThrownBy(() -> client.authenticate(ACCESS_TOKEN))
                .isInstanceOf(BlackDuckException.class)
                .hasMessageContaining("401");
    }

    @Test
    void retrievesServerVersion() throws Exception {
        server.enqueue(new MockResponse().setBody(new JSONObject().put("version", VERSION).toString()));

        final var version = client.getServerVersion();

        assertThat(version).isEqualTo(VERSION);
        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/api/current-version");
    }

    @Test
    void readsProjectComponents() throws Exception {
        server.enqueue(new MockResponse().setBody(new JSONObject().put("items", new JSONArray()
                .put(new JSONObject()
                        .put("componentName", NAME))).toString()));

        final var components = client.getComponents(PROJECT_ID, VERSION_ID);

        assertThat(components).hasSize(1);
        assertThat(components.get(0).componentName).isEqualTo(NAME);
        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/api/projects/" + PROJECT_ID + "/versions/" + VERSION_ID + "/components");
    }

    @Test
    void readsOriginDependencies() throws Exception {
        final var href = String.format("https://server/api/components/%s/versions/%s/origins/%s", COMPONENT_ID, VERSION_ID, ORIGIN_ID);
        final var origin = new BlackDuckApi.OriginJson();
        origin.origin = URI.create(href);
        server.enqueue(new MockResponse().setBody(new JSONObject().put("items", new JSONArray()
                .put(new JSONObject().put("_meta", new JSONObject().put("href", href)))).toString()));

        final var deps = client.getDependencies(origin);

        assertThat(deps).hasSize(1);
        assertThat(deps.get(0).getId()).isEqualTo(ORIGIN_ID);
        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/api/components/" + COMPONENT_ID + "/versions/" + VERSION_ID + "/origins/" + ORIGIN_ID
                + "/direct-dependencies");
    }

    @Nested
    class FindProject {
        @Test
        void findsByNameFragment() throws Exception {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject().put("name", PROJECT)
                                    .put("_meta", new JSONObject()
                                            .put("href", URI.create("https://server/something/" + PROJECT_ID))))).toString()));

            final var project = client.findProject(PROJECT).orElseThrow();

            assertThat(project.getId()).isEqualTo(PROJECT_ID);
            assertThat(project.name).isEqualTo(PROJECT);
            final var request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/projects?q=name%3A" + PROJECT);
        }

        @Test
        void findsNothing_emptyList() {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()).toString()));

            assertThat(client.findProject(PROJECT)).isEmpty();
        }

        @Test
        void findsNothing_multipleMatches() {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject())
                            .put(new JSONObject())).toString()));

            assertThat(client.findProject(PROJECT)).isEmpty();
        }
    }

    @Nested
    class FindProjectVersion {
        @Test
        void findsByName() throws Exception {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject().put("name", PROJECT).put("versionName", VERSION)
                                    .put("_meta", new JSONObject()
                                            .put("href", URI.create("https://server/something/" + VERSION_ID))))).toString()));

            final var projectVersion = client.findProjectVersion(PROJECT_ID, VERSION).orElseThrow();

            assertThat(projectVersion.getId()).isEqualTo(VERSION_ID);
            assertThat(projectVersion.versionName).isEqualTo(VERSION);
            final var request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/projects/" + PROJECT_ID + "/versions?q=versionName%3A" + VERSION);
        }

        @Test
        void findsNothing_emptyList() {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()).toString()));

            assertThat(client.findProject(NAME)).isEmpty();
        }

        @Test
        void findsNothing_multipleMatches() {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject())
                            .put(new JSONObject())).toString()));

            assertThat(client.findProject(NAME)).isEmpty();
        }
    }
}
