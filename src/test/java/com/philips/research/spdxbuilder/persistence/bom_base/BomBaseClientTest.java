/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import com.github.packageurl.PackageURL;
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

class BomBaseClientTest {
    private static final String PURL = "pkg:namespace/name@version";
    private static final String TITLE = "Title";
    private static final int PORT = 1080;
    private final MockWebServer mockServer = new MockWebServer();

    private final BomBaseClient client = new BomBaseClient(URI.create("http://localhost:" + PORT));

    @BeforeEach
    void setUp() throws IOException {
        mockServer.start(PORT);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void getPackageMetadata() throws Exception {
        mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                .put("attributes", new JSONObject()
                        .put("title", TITLE)).toString()));

        final var meta = client.readPackage(new PackageURL(PURL)).orElseThrow();

        assertThat(meta.getTitle()).contains(TITLE);
        final var request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/packages/pkg%253Anamespace%252Fname%2540version");
    }

    @Test
    void throws_errorStatus() {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> client.readPackage(new PackageURL(PURL)))
                .isInstanceOf(BomBaseException.class)
                .hasMessageContaining("responded with status 404");
    }
}
