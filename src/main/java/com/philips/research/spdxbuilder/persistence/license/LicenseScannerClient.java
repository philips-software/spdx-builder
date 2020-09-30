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
import com.philips.research.spdxbuilder.core.bom.Package;
import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<LicenseInfo> scanLicense(Package pkg) {
        return query(() -> {
            String v = (!pkg.getVersion().isEmpty()) ? pkg.getVersion() : " ";
            final var body = new RequestJson(pkg.getLocation().orElse(null));
            final var resp = rest.scan(pkg.getNamespace(), pkg.getName(), v, body).execute();
            if (!resp.isSuccessful()) {
                throw new LicenseScannerException("License scanner responded with status " + resp.code());
            }

            final @NullOr ResultJson result = resp.body();
            if (result == null || result.license == null) {
                return Optional.empty();
            }

            pkg.getDeclaredLicense()
                    .filter(l -> !result.confirmed && !l.equals(result.license))
                    .ifPresent(l -> contest(result.id));

            return Optional.of(new LicenseInfo(result.license, result.confirmed));
        });
    }

    private void contest(@NullOr UUID scanId) {
        if (scanId != null) {
            query(() -> rest.contest(scanId).execute());
        }
    }

    private <R> R query(Request<R> supplier) {
        try {
            return supplier.get();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON formatting error", e);
        } catch (IOException e) {
            throw new LicenseScannerException("The license scanner is not reachable at " + licenseServer);
        }
    }

    @FunctionalInterface
    interface Request<R> {
        R get() throws IOException;
    }
}

