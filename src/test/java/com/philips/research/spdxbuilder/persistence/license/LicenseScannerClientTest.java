/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.domain.Package;
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
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "My/#?Namespace";
    private static final String NAME = "My/#?Name";
    private static final String VERSION = "My/#?Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "Apache-2.0";
    private static final String SCAN_ID = "ScanId";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MockWebServer mockServer = new MockWebServer();

    private final LicenseScannerClient client = new LicenseScannerClient(URI.create("http://localhost:" + PORT));
    private final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);

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
        pkg.setSourceLocation(LOCATION);
        mockServer.enqueue(new MockResponse().setBody(
                new JSONObject()
                        .put("license", LICENSE)
                        .put("confirmed", true).toString()));

        final var license = client.scanLicense(pkg).get();

        final var request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/packages");
        assertThat(MAPPER.readTree(request.getBody().readUtf8()))
                .isEqualTo(MAPPER.readTree(new JSONObject()
                        .put("purl", pkg.getPurl())
                        .put("location", LOCATION).toString()));
        assertThat(license.getLicense()).contains(LICENSE);
        assertThat(license.isConfirmed()).isTrue();
    }

    @Test
    void ignoresEmptyLicense() {
        mockServer.enqueue(new MockResponse().setBody("{}"));

        final var license = client.scanLicense(pkg);

        assertThat(license).isEmpty();
    }

    @Test
    void contestsScan_differentFromDeclaredLicense() throws Exception {
        pkg.setDeclaredLicense("Other");
        mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                .put("id", SCAN_ID)
                .put("license", LICENSE).toString()));
        mockServer.enqueue(new MockResponse());

        client.scanLicense(pkg);

        mockServer.takeRequest();
        final var contestRequest = mockServer.takeRequest();
        assertThat(contestRequest.getMethod()).isEqualTo("POST");
        assertThat(contestRequest.getPath()).isEqualTo(String.format("/scans/%s/contest", SCAN_ID));
        assertThat(contestRequest.getBody().readUtf8()).isEqualTo(new JSONObject().put("license", "Other").toString());
    }

    @Test
    void ignores_serverNotReachable() {
        var serverlessClient = new LicenseScannerClient(URI.create("http://localhost:1234"));

        assertThatThrownBy(() -> serverlessClient.scanLicense(pkg))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    void throws_unexpectedResponseFromServer() {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> client.scanLicense(pkg))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status 404");
    }
}
