/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.philips.research.spdxbuilder.persistence.license_scanner.LicenseScannerApi.ContestJson;
import com.philips.research.spdxbuilder.persistence.license_scanner.LicenseScannerApi.RequestJson;
import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * REST client for the License Scanner Service.
 *
 * @see <a href="https://github.com/philips-labs/license-scanner">License Scanner Service</a>
 */
public class LicenseScannerClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final URI licenseServer;
    private final LicenseScannerApi rest;

    public LicenseScannerClient(URI licenseServer) {
        this.licenseServer = licenseServer;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(licenseServer.toASCIIString())
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .build();
        rest = retrofit.create(LicenseScannerApi.class);
    }

    /**
     * Queries the licenses for a single package.
     *
     * @return detected license for the package
     */
    public Optional<LicenseInfo> scanLicense(URI purl, @NullOr URI location) {
        final var body = new RequestJson(purl, location);

        return query(rest.scan(body))
                .filter(r -> r.license != null)
                .map(r -> new LicenseInfo(r.license, r.confirmed));
    }

    public void contest(URI purl, String license) {
        final var scanId = URLEncoder.encode(purl.toASCIIString(), StandardCharsets.UTF_8);
        query(rest.contest(scanId, new ContestJson(license)));
    }

    private <T> Optional<T> query(Call<T> query) {
        try {
            final var response = query.execute();
            if (!response.isSuccessful()) {
                throw new LicenseScannerException("License scanner responded with status " + response.code());
            }
            return Optional.ofNullable(response.body());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON formatting error", e);
        } catch (IOException e) {
            throw new LicenseScannerException("The license scanner is not reachable at " + licenseServer);
        }
    }

    static class LicenseInfo {
        private final String license;
        private final boolean confirmed;

        public LicenseInfo(String license, boolean confirmed) {
            this.license = license;
            this.confirmed = confirmed;
        }

        public String getLicense() {
            return license;
        }

        public boolean isConfirmed() {
            return confirmed;
        }
    }
}

