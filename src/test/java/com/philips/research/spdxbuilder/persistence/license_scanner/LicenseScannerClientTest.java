/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.spdxbuilder.core.BusinessException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseScannerClientTest {
    private static final int PORT = 1080;
    private static final URI PURL = URI.create("pkg:namespace/name@version");
    private static final String SCAN_ID = "pkg%253Anamespace%252Fname%2540version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "Apache-2.0";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MockWebServer mockServer = new MockWebServer();

    private final LicenseScannerClient client = new LicenseScannerClient(URI.create("http://localhost:" + PORT));

    @BeforeEach
    void setUp() throws IOException {
        mockServer.start(PORT);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void queriesLicense() throws Exception {
        mockServer.enqueue(new MockResponse().setBody(
                new JSONObject()
                        .put("license", LICENSE)
                        .put("confirmed", true).toString()));

        final var license = client.scanLicense(PURL, LOCATION).get();

        final var request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/packages");
        assertThat(MAPPER.readTree(request.getBody().readUtf8()))
                .isEqualTo(MAPPER.readTree(new JSONObject()
                        .put("purl", PURL)
                        .put("location", LOCATION).toString()));
        assertThat(license.getLicense()).contains(LICENSE);
        assertThat(license.isConfirmed()).isTrue();
    }

    @Test
    void ignoresEmptyLicense() {
        mockServer.enqueue(new MockResponse().setBody("{}"));

        final var license = client.scanLicense(PURL, LOCATION);

        assertThat(license).isEmpty();
    }

    @Test
    void contestsLicense() throws Exception {
        mockServer.enqueue(new MockResponse());

        client.contest(PURL, LICENSE);

        final var contestRequest = mockServer.takeRequest();
        assertThat(contestRequest.getMethod()).isEqualTo("POST");
        assertThat(contestRequest.getPath()).isEqualTo(String.format("/scans/%s/contest", SCAN_ID));
        assertThat(contestRequest.getBody().readUtf8()).isEqualTo(new JSONObject().put("license", LICENSE).toString());
    }

    @Test
    void ignores_serverNotReachable() {
        var serverlessClient = new LicenseScannerClient(URI.create("http://localhost:1234"));

        assertThatThrownBy(() -> serverlessClient.scanLicense(PURL, LOCATION))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    void throws_unexpectedResponseFromServer() {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> client.scanLicense(PURL, LOCATION))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status 404");
    }

    @Test
    void throws_mapformedResponseFromServer() {
        mockServer.enqueue(new MockResponse().setBody("Not a JSON response"));

        assertThatThrownBy(() -> client.scanLicense(PURL, LOCATION))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
