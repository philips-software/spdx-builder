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

import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.bom.Package;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class LicenseScannerClientTest {
    private static final int PORT = 1080;
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "Apache-2.0";
    private static final UUID SCAN_ID = UUID.randomUUID();

    private static ClientAndServer mockServer;

    private final LicenseScannerClient client = new LicenseScannerClient(URI.create("http://localhost:" + PORT));
    private final Package pkg = new Package("Type", NAMESPACE, NAME, VERSION);

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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void queriesLicense() {
        pkg.setLocation(LOCATION);
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages/%s/%s/%s", NAMESPACE, NAME, VERSION))
                .withBody(new JSONObject().put("location", LOCATION).toString())
                .withContentType(MediaType.APPLICATION_JSON_UTF_8))
                .respond(response().withStatusCode(200).withBody(new JSONObject()
                        .put("license", LICENSE)
                        .put("confirmed", true).toString()));

        final var license = client.scanLicense(pkg).get();

        assertThat(license.getLicense()).contains(LICENSE);
        assertThat(license.isConfirmed()).isTrue();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void queriesLicenseForEmptyNamespace() {
        final var noNamespace = new Package(TYPE, "", NAME, VERSION);
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages//%s/%s", NAME, VERSION)))
                .respond(response().withStatusCode(200)
                        .withBody(new JSONObject().put("license", LICENSE).toString()));

        final var licenses = client.scanLicense(noNamespace).get();

        assertThat(licenses.getLicense()).isEqualTo(LICENSE);
    }

    @Test
    void ignoresEmptyLicense() {
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages/%s/%s/%s", NAMESPACE, NAME, VERSION)))
                .respond(response().withStatusCode(200).withBody("{}"));

        assertThat(client.scanLicense(pkg)).isEmpty();
    }

    @Test
    void contestsScan_differentFromDeclaredLicense() {
        pkg.setDeclaredLicense("Other");
        mockServer.when(request().withMethod("POST")
                .withPath(String.format("/packages/%s/%s/%s", NAMESPACE, NAME, VERSION)))
                .respond(response().withStatusCode(200)
                        .withBody(new JSONObject()
                                .put("id", SCAN_ID)
                                .put("license", LICENSE).toString()));

        client.scanLicense(pkg);

        mockServer.verify(request().withMethod("POST")
                .withPath(String.format("/scans/%s/contest", SCAN_ID)));
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
        // Default not-found response
        assertThatThrownBy(() -> client.scanLicense(pkg))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status 404");
    }
}
