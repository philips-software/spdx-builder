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
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.io.IOException;
import java.net.URI;
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

    private final URI licenseServer;
    private final RestClient rest;

    public LicenseScannerClient(URI licenseServer) {
        this.licenseServer = licenseServer;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(licenseServer.toASCIIString())
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .build();
        rest = retrofit.create(RestClient.class);
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
            if (version.isEmpty()) {
                version = " ";
            }
            final var resp = rest.scan(namespace, name, version, body).execute();
            if (!resp.isSuccessful()) {
                throw new LicenseScannerException("License scanner responded with status " + resp.code());
            }

            final @NullOr ResultJson result = resp.body();
            if (result == null || result.license == null) {
                return Optional.empty();
            }
            return Optional.of(new LicenseInfo(result.license, result.confirmed));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON formatting error", e);
        } catch (IOException e) {
            throw new LicenseScannerException("The license scanner is not reachable at " + licenseServer);
        }
    }

    /**
     * Retrofit REST API declaration.
     */
    interface RestClient {
        /**
         * Start scanning a package or retrieve result from an earlier scan.
         *
         * @return scan result with or without a concluded license
         */
        @POST("/packages/{namespace}/{name}/{version}")
        Call<ResultJson> scan(@Path("namespace") String namespace,
                              @Path("name") String name,
                              @Path("version") String version,
                              @Body RequestJson body);
    }
}

