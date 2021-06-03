/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlackDuckClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    private final URL url;

    private final BlackDuckApi api;
    private @NullOr String bearerToken;

    public BlackDuckClient(URL url, boolean skipSSL) {
        this.url = url;
        final var client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    if (bearerToken == null) {
                        return chain.proceed(chain.request());
                    }
                    final var newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "bearer " + bearerToken)
                            .build();
                    return chain.proceed(newRequest);
                });
        if (skipSSL) {
            disableSSL(client);
        }
        final var retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .client(client.build())
                .build();

        api = retrofit.create(BlackDuckApi.class);
    }

    private void disableSSL(OkHttpClient.Builder client) {
        try {
            final var gullibleBeliever = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };
            final var sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{gullibleBeliever}, new java.security.SecureRandom());
            client.sslSocketFactory(sslContext.getSocketFactory(), gullibleBeliever)
                    .hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new RuntimeException("SSL bypass failed", e);
        }
    }

    void authenticate(String token) {
        query(api.authenticate("token " + token))
                .ifPresent(auth -> bearerToken = auth.bearerToken);
    }

    String getServerVersion() {
        return query(api.serverVersion()).orElseThrow().version;
    }

    Optional<BlackDuckProduct> findProject(String name) {
        //noinspection unchecked
        return query(api.findProjects("name:" + name))
                .flatMap(result -> matches((Collection<BlackDuckProduct>) (Object) result.items, name));
    }

    Optional<BlackDuckProduct> findProjectVersion(UUID projectId, String name) {
        //noinspection unchecked
        return  query(api.findProjectVersions(projectId, "versionName:" + name))
                .flatMap(result -> matches((Collection<BlackDuckProduct>) (Object) result.items, name));
    }

    private Optional<BlackDuckProduct> matches(Collection<BlackDuckProduct> items, String name) {
        if (items.size() <= 1) {
            return items.stream().findFirst();
        }
        return items.stream().filter(item -> name.equals(item.getName())).findAny();
    }

    List<BlackDuckComponent> getRootComponents(UUID projectId, UUID versionId) {
        //noinspection unchecked
        final var components = query(api.getRootComponentVersions(projectId, versionId))
                .map(object -> (List<BlackDuckComponent>) (List<? extends BlackDuckComponent>) object.items)
                .orElse(List.of());
        //noinspection unchecked
        final var subprojects = query(api.getBomComponents(projectId, versionId))
                .map(object -> (List<BlackDuckComponent>) (List<? extends BlackDuckComponent>) object.items)
                .orElse(List.of()).stream()
                .filter(BlackDuckComponent::isSubproject)
                .collect(Collectors.toList());
        components.addAll(subprojects);
        return components;
    }

    List<BlackDuckComponent> getDependencies(UUID projectId, UUID versionId, BlackDuckComponent component) {
        //noinspection unchecked
        return query(api.getChildComponentVersions(projectId, versionId,
                component.getId(), component.getVersionId(), component.getHierarchicalId()))
                .map(object -> (List<BlackDuckComponent>) (List<? extends BlackDuckComponent>) object.items)
                .orElse(List.of());
    }

    BlackDuckComponentDetails getComponentDetails(BlackDuckComponent component) {
        return query(api.getComponent(component.getId())).orElseThrow();
    }

    <T> Optional<T> query(Call<T> request) {
        try {
            final var response = request.execute();
            if (response.isSuccessful()) {
                return Optional.ofNullable(response.body());
            }
            throw new BlackDuckException("Server responded with status " + response.code() + " " + response.message());
        } catch (IOException e) {
            throw new BlackDuckException("Failed to connect to Black Duck server on " + url, e);
        }
    }
}
