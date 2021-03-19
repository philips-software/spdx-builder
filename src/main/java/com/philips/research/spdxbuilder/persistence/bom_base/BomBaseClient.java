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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.packageurl.PackageURL;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class BomBaseClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final URI server;
    private final BomBaseApi rest;

    BomBaseClient(URI server) {
        this.server = server;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(server.toASCIIString())
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .build();
        rest = retrofit.create(BomBaseApi.class);
    }

    Optional<PackageMetadata> readPackage(PackageURL purl) {
        final var param = encode(purl.canonicalize());
        return query(rest.getPackage(param)).map(meta -> meta);
    }

    private String encode(String uri) {
        return URLEncoder.encode(uri, StandardCharsets.UTF_8);
    }

    private <T> Optional<T> query(Call<T> query) {
        try {
            final var response = query.execute();
            if (!response.isSuccessful()) {
                throw new BomBaseException("BOM-base server responded with status " + response.code());
            }
            return Optional.ofNullable(response.body());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON formatting error", e);
        } catch (IOException e) {
            throw new BomBaseException("The BOM-base knowledge base is not reachable at " + server);
        }
    }

}
