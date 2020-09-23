/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static com.philips.research.spdxbuilder.core.ConversionStore.LicenseInfo;

/**
 * REST client for the License Scanner Service.
 *
 * @see <a href="https://github.com/philips-labs/license-scanner">License Scanner Service</a>
 */
public class LicenseScannerClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json; charset=utf-8";

    private final URI licenseServer;
    private final HttpClient client = HttpClient.newBuilder().build();

    public LicenseScannerClient(URI licenseServer) {
        this.licenseServer = licenseServer;
    }

    /**
     * Queries the licenses for a single package.
     *
     * @param namespace namespace of the package
     * @param name      name of the package within the namespace
     * @param version   version of the package
     * @param location  (optional) location of the source code for the package
     * @return detected license for the package
     */
    public Optional<LicenseInfo> scanLicense(String namespace, String name, String version, @NullOr URI location) {
        try {
            final var body = new RequestJson(location);
            final var response = post(body, "/packages/%s/%s/%s", namespace, name, version);
            if (response.statusCode() != 200) {
                throw new LicenseScannerException("License scanner returned unexpected response: status " + response.statusCode());
            }
            final var result = MAPPER.readValue(response.body(), ResultJson.class);
            if (result.license == null) {
                return Optional.empty();
            }
            return Optional.of(new LicenseInfo(result.license, result.confirmed));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON formatting error", e);
        }
    }

    private HttpResponse<String> post(Object body, String path, Object... params) {
        try {
            final var json = MAPPER.writeValueAsString(body);
            final var url = String.format(path, params);
            final var request = HttpRequest.newBuilder(licenseServer.resolve(url))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", MEDIA_TYPE_APPLICATION_JSON)
                    .header("Accept", MEDIA_TYPE_APPLICATION_JSON)
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            throw new LicenseScannerException("License scanner is not reachable at " + licenseServer);
        }
    }
}
