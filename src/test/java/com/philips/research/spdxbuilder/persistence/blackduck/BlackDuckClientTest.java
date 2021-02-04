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
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackDuckClientTest {
    private static final UUID ACCESS_TOKEN = UUID.randomUUID();
    private static final String BEARER_TOKEN = "BearerToken";
    private static final String VERSION = "1.2.3";
    private static final int PORT = 1080;

    private final MockWebServer server = new MockWebServer();
    private BlackDuckClient client;
    private URL url;

    @BeforeEach
    void setUp() throws IOException {
        server.start(PORT);
        url = server.url("api/").url();
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
        assertThat(auth.getHeader("Authorization")).isEqualTo("Token " + ACCESS_TOKEN);
        final var request = server.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + BEARER_TOKEN);
    }

    @Test
    void throws_authenticationFailure() {
        server.enqueue(new MockResponse().setResponseCode(401));

        assertThatThrownBy(() -> client.authenticate(ACCESS_TOKEN))
                .isInstanceOf(BlackDuckException.class)
                .hasMessageContaining("401");
    }
}
