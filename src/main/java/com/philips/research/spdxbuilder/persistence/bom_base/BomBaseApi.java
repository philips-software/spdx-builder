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

import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface BomBaseApi {
    @GET("/packages/{purl}")
    Call<PackageJson> getPackage(@Path("purl") String purl);

    class PackageJson implements PackageMetadata {
        Map<String, Object> attributes = new HashMap<>();

        @Override
        public Optional<String> getTitle() {
            return getStringAttribute("title");
        }

        @Override
        public Optional<String> getDescription() {
            return getStringAttribute("description");
        }

        @Override
        public Optional<URL> getHomePage() {
            return getUrlAttribute("home_page");
        }

        @Override
        public Optional<String> getAttribution() {
            return getStringAttribute("attribution");
        }

        @Override
        public Optional<String> getSupplier() {
            return getStringAttribute("supplier");
        }

        @Override
        public Optional<String> getOriginator() {
            return getStringAttribute("originator");
        }

        @Override
        public Optional<URI> getDownloadLocation() {
            return getUriAttribute("download_location");
        }

        @Override
        public Optional<String> getSha1() {
            return getStringAttribute("sha1");
        }

        @Override
        public Optional<String> getSha256() {
            return getStringAttribute("sha256");
        }

        @Override
        public Optional<URI> getSourceLocation() {
            return getUriAttribute("source_location");
        }

        @Override
        public Optional<String> getDeclaredLicense() {
            return getStringAttribute("declared_license");
        }

        @Override
        public Optional<String> getDetectedLicense() {
            return getStringAttribute("detected_license");
        }

        private Optional<String> getStringAttribute(String tag) {
            return getAttribute(tag, str -> (String) str);
        }

        private Optional<URI> getUriAttribute(String tag) {
            return getAttribute(tag, str -> URI.create((String) str));
        }

        private Optional<URL> getUrlAttribute(String tag) {
            return getAttribute(tag, str -> {
                try {
                    return URI.create((String) str).toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            });
        }

        private <T> Optional<T> getAttribute(String tag, Function<Object, T> converter) {
            final @NullOr Object value = attributes.get(tag);
            try {
                if (value == null) {
                    return Optional.empty();
                }
                return Optional.of(converter.apply(value));
            } catch (Exception e) {
                System.err.println("WARNING: Attribute " + tag + " value '" + value + "' has an incompatible format");
                return Optional.empty();
            }
        }
    }
}
