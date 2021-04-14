/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.BusinessException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadClientTest {
    private static final File FILE = new File("src/test/resources/test.txt");
    private static final int PORT = 1080;
    private static final String PATH = "/5path/to/upload/";

    private final MockWebServer mockServer = new MockWebServer();

    @BeforeEach
    void setUp() throws IOException {
        mockServer.start(PORT);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void uploadsFile() throws Exception {
        mockServer.enqueue(new MockResponse());
        final var client = new UploadClient(mockServer.url(PATH).uri());

        client.upload(FILE);

        final var request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo(PATH);
        assertThat(request.getHeader("Content-Type")).contains("multipart/form-data");
    }

    @Test
    void ignores_serverNotReachable() {
        var serverlessClient = new UploadClient(URI.create("http://localhost:1234"));

        assertThatThrownBy(() -> serverlessClient.upload(FILE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    void throws_unexpectedResponseFromServer() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(404));
        final var client = new UploadClient(mockServer.url(PATH).uri());

        // Default not-found response
        assertThatThrownBy(() -> client.upload(FILE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status 404");
    }
}
