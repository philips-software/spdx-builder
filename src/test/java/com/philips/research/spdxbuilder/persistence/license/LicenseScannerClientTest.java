/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import com.philips.research.spdxbuilder.core.BusinessException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class LicenseScannerClientTest {
    private static final int PORT = 1080;
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "Apache-2.0";

    private static ClientAndServer mockServer;

    private final LicenseScannerClient client = new LicenseScannerClient(URI.create("http://localhost:" + PORT));

    @BeforeAll
    static void beforeAll() {
        mockServer = startClientAndServer(PORT);
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @AfterEach
    void afterEach() {
        mockServer.reset();
    }

    @Test
    void queriesLicense() {
        final var request = new JSONObject()
                .put("location", LOCATION);
        final var response = new JSONObject()
                .put("license", LICENSE)
                .put("confirmed", true);
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages/%s/%s/%s", NAMESPACE, NAME, VERSION))
                .withBody(request.toString()).withContentType(MediaType.APPLICATION_JSON_UTF_8))
                .respond(response().withStatusCode(200).withBody(response.toString()));

        @SuppressWarnings("OptionalGetWithoutIsPresent") final var license = client.scanLicense(NAMESPACE, NAME, VERSION, LOCATION).get();

        assertThat(license.getLicense()).contains(LICENSE);
        assertThat(license.isConfirmed()).isTrue();
    }

    @Test
    void queriesLicenseForEmptyNamespace() {
        final var request = new JSONObject()
                .put("location", LOCATION);
        final var response = new JSONObject()
                .put("license", LICENSE);
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages//%s/%s", NAME, VERSION))
                .withBody(request.toString()).withContentType(MediaType.APPLICATION_JSON_UTF_8))
                .respond(response().withStatusCode(200).withBody(response.toString()));

        @SuppressWarnings("OptionalGetWithoutIsPresent") final var licenses = client.scanLicense("", NAME, VERSION, LOCATION).get();

        assertThat(licenses.getLicense()).isEqualTo(LICENSE);
    }

    @Test
    void ignoresEmptyLicense() {
        final var request = new JSONObject()
                .put("location", LOCATION);
        final var response = new JSONObject();
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages/%s/%s/%s", NAMESPACE, NAME, VERSION))
                .withBody(request.toString()).withContentType(MediaType.APPLICATION_JSON_UTF_8))
                .respond(response().withStatusCode(200).withBody(response.toString()));

        @SuppressWarnings("OptionalGetWithoutIsPresent") final var license = client.scanLicense(NAMESPACE, NAME, VERSION, LOCATION);

        assertThat(license).isEmpty();
    }

    @Test
    void ignores_serverNotReachable() {
        var serverlessClient = new LicenseScannerClient(URI.create("http://localhost:1234"));

        assertThatThrownBy(() -> serverlessClient.scanLicense(NAMESPACE, NAME, VERSION, LOCATION))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    void throws_unexpectedResponseFromServer() {
        // Default not-found response
        assertThatThrownBy(() -> client.scanLicense(NAMESPACE, NAME, VERSION, LOCATION))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status 404");
    }
}
