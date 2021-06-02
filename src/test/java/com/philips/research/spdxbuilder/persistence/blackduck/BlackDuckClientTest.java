/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.License;
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
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlackDuckClientTest {
    private static final String PROJECT = "Project";
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final String VERSION = "1.2.3";
    private static final UUID VERSION_ID = UUID.randomUUID();
    private static final UUID COMPONENT_ID = UUID.randomUUID();
    private static final UUID COMPONENT_VERSION_ID = UUID.randomUUID();
    private static final String DESCRIPTION = "Description";
    private static final String LICENSE = "Apache-2.0";
    private static final int PORT = 1080;

    private final MockWebServer server = new MockWebServer();
    private BlackDuckClient client;

    @BeforeEach
    void setUp() throws IOException {
        server.start(PORT);
        final var url = server.url("/").url();
        client = new BlackDuckClient(url, false);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
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

    @Nested
    class Authentication {
        private static final String ACCESS_TOKEN = "AccessToken";
        private static final String BEARER_TOKEN = "BearerToken";

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
    }

    @Nested
    class FindProject {
        @Test
        void findsByNameFragment() throws Exception {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject()
                                    .put("name", PROJECT)
                                    .put("description", DESCRIPTION)
                                    .put("_meta", new JSONObject()
                                            .put("href", URI.create("https://server/something/" + PROJECT_ID))))).toString()));

            final var project = client.findProject(PROJECT).orElseThrow();

            assertThat(project.getId()).isEqualTo(PROJECT_ID);
            assertThat(project.getName()).isEqualTo(PROJECT);
            assertThat(project.getDescription()).contains(DESCRIPTION);
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
                            .put(new JSONObject()
                                    .put("name", PROJECT)
                                    .put("versionName", VERSION)
                                    .put("releaseComments", DESCRIPTION)
                                    .put("license", new JSONObject().put("spdxId", LICENSE))
                                    .put("_meta", new JSONObject()
                                            .put("href", URI.create("https://server/something/" + VERSION_ID))))).toString()));

            final var projectVersion = client.findProjectVersion(PROJECT_ID, VERSION).orElseThrow();

            assertThat(projectVersion.getId()).isEqualTo(VERSION_ID);
            assertThat(projectVersion.getName()).isEqualTo(VERSION);
            assertThat(projectVersion.getDescription()).contains(DESCRIPTION);
            assertThat(projectVersion.getLicense()).contains(License.of(LICENSE));
            final var request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/projects/" + PROJECT_ID + "/versions?q=versionName%3A" + VERSION);
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
    class ReadBillOfMaterials {
        private static final String COMPONENT_NAME = "Component Name";
        private static final String COMPONENT_VERSION_NAME = "Component Version Name";
        private static final long HIERARCHY_ID = 1234567890;
        private static final String USAGE = "Usage";
        private final MockResponse EMPTY_LIST_RESPONSE = new MockResponse()
                .setBody(new JSONObject().put("Items", new JSONArray()).toString());

        @Test
        void readsProjectRootComponents() throws Exception {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject()
                                    .put("componentName", COMPONENT_NAME)
                                    .put("componentVersionName", COMPONENT_VERSION_NAME)
                                    .put("componentVersion", "api/components/" + COMPONENT_ID + "/versions/" + COMPONENT_VERSION_ID + "/")
                                    .put("usages", new JSONArray()
                                            .put(USAGE))
                                    .put("origins", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("externalNamespace", "maven")
                                                    .put("externalId", "group:id:version")))
                                    .put("_meta", new JSONObject()
                                            .put("links", new JSONArray()
                                                    .put(new JSONObject()
                                                            .put("rel", "children")
                                                            .put("href", "api/projects/etc/" + HIERARCHY_ID + "/children"))))))
                    .toString()));
            server.enqueue(EMPTY_LIST_RESPONSE); // No subprojects

            final var components = client.getRootComponents(PROJECT_ID, VERSION_ID);

            assertThat(components).hasSize(1);
            final var comp = components.get(0);
            assertThat(comp.isSubproject()).isFalse();
            assertThat(comp.getName()).isEqualTo(COMPONENT_NAME);
            assertThat(comp.getId()).isEqualTo(COMPONENT_ID);
            assertThat(comp.getVersion()).isEqualTo(COMPONENT_VERSION_NAME);
            assertThat(comp.getVersionId()).isEqualTo(COMPONENT_VERSION_ID);
            assertThat(comp.getHierarchicalId()).isEqualTo(HIERARCHY_ID);
            assertThat(comp.getUsages()).contains(USAGE);
            assertThat(comp.getPackageUrls()).contains(new PackageURL("pkg:maven/group/id@version"));
            final var request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/projects/" + PROJECT_ID + "/versions/" + VERSION_ID
                    + "/hierarchical-components?limit=9999");
        }

        @Test
        void readsComponentDependencies() throws Exception {
            final var parent = mock(BlackDuckComponent.class);
            when(parent.getId()).thenReturn(COMPONENT_ID);
            when(parent.getVersionId()).thenReturn(COMPONENT_VERSION_ID);
            when(parent.getHierarchicalId()).thenReturn(HIERARCHY_ID);
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray().put(new JSONObject()
                            .put("componentName", COMPONENT_NAME)))
                    .toString()));

            final var children = client.getDependencies(PROJECT_ID, VERSION_ID, parent);

            assertThat(children).hasSize(1);
            final var child = children.get(0);
            assertThat(child.getName()).isEqualTo(COMPONENT_NAME);
            final var request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/projects/" + PROJECT_ID + "/versions/" + VERSION_ID
                    + "/components/" + COMPONENT_ID + "/versions/" + COMPONENT_VERSION_ID
                    + "/hierarchical-components/" + HIERARCHY_ID + "/children?limit=999");
        }

        @Test
        void readsSubprojectsAsComponents() {
            final var projectId = UUID.randomUUID();
            final var versionId = UUID.randomUUID();
            server.enqueue(EMPTY_LIST_RESPONSE);
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("items", new JSONArray()
                            .put(new JSONObject()
                                    .put("componentName", "Ignore me!")
                                    .put("componentType", "KB_COMPONENT"))
                            .put(new JSONObject()
                                    .put("componentType", "SUB_PROJECT")
                                    .put("componentVersion", "api/etc/components/" + projectId + "/versions/" + versionId)))
                    .toString()));

            final var components = client.getRootComponents(PROJECT_ID, VERSION_ID);

            assertThat(components).hasSize(1);
            final var subproject = components.get(0);
            assertThat(subproject.isSubproject()).isTrue();
            assertThat(subproject.getId()).isEqualTo(projectId);
            assertThat(subproject.getVersionId()).isEqualTo(versionId);
        }
    }

    @Nested
    class ReadKnowledgeBase {
        private final String HOMEPAGE = "https://homepage.com";

        private final BlackDuckComponent component = mock(BlackDuckComponent.class);

        @BeforeEach
        void beforeEach() {
            when(component.getId()).thenReturn(COMPONENT_ID);
        }

        @Test
        void readsComponentDetails() throws Exception {
            server.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("description", DESCRIPTION)
                    .put("url", HOMEPAGE)
                    .toString()));

            final var details = client.getComponentDetails(component);

            assertThat(details.getDescription()).contains(DESCRIPTION);
            assertThat(details.getHomepage()).contains(new URL(HOMEPAGE));
            final var request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/components/" + COMPONENT_ID);
        }
    }
}
