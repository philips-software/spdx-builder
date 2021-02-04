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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import okhttp3.OkHttpClient;
import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class BlackDuckClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    private final URL url;

    private BlackDuckApi api;
    private @NullOr String bearerToken;

    public BlackDuckClient(URL url) {
        this.url = url;
        final var client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    if (bearerToken == null) {
                        return chain.proceed(chain.request());
                    }
                    final var newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + bearerToken)
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();
        final var retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .client(client)
                .build();

        api = retrofit.create(BlackDuckApi.class);
    }

    void authenticate(UUID token) {
        final var auth = query(api.authenticate("Token " + token));
        this.bearerToken = auth.bearerToken;
    }

    String getServerVersion() {
        return query(api.serverVersion()).version;
    }

    <T> T query(Call<T> request) {
        try {
            final var response = request.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            throw new BlackDuckException("Server responded with status " + response.code() + " " + response.message());
        } catch (IOException e) {
            throw new BlackDuckException("Failed to connect to Black Duck server on " + url);
        }
    }
}
