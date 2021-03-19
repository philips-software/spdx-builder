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

import com.philips.research.spdxbuilder.persistence.bom_base.BomBaseApi.PackageJson;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class BomBaseApiTest {
    @Nested
    class AttributeConversion {
        public static final String HOMEPAGE_URL = "https://example.com/home";
        public static final String DOWNLOAD_URI = "http:example.com/download";
        public static final String SOURCE_URI = "http://example.com/sources";
        private static final String TITLE = "title";
        private static final String DESCRIPTION = "description";
        private static final String HOMEPAGE = "home_page";
        private static final String ATTRIBUTION = "attribution";
        private static final String DOWNLOAD_LOCATION = "download_location";
        private static final String SHA1 = "sha1";
        private static final String SHA256 = "sha256";
        private static final String SOURCE_LOCATION = "source_location";
        private static final String DECLARED_LICENSE = "declared_license";
        private static final String DETECTED_LICENSE = "detected_license";
        final PackageJson meta = new PackageJson();

        @Test
        void extractsAttributeValues() throws Exception {
            meta.attributes.put(TITLE, TITLE);
            meta.attributes.put(DESCRIPTION, DESCRIPTION);
            meta.attributes.put(HOMEPAGE, HOMEPAGE_URL);
            meta.attributes.put(ATTRIBUTION, ATTRIBUTION);
            meta.attributes.put(DOWNLOAD_LOCATION, DOWNLOAD_URI);
            meta.attributes.put(SHA1, SHA1);
            meta.attributes.put(SHA256, SHA256);
            meta.attributes.put(SOURCE_LOCATION, SOURCE_URI);
            meta.attributes.put(DECLARED_LICENSE, DECLARED_LICENSE);
            meta.attributes.put(DETECTED_LICENSE, DETECTED_LICENSE);

            assertThat(meta.getTitle()).contains(TITLE);
            assertThat(meta.getDescription()).contains(DESCRIPTION);
            assertThat(meta.getHomePage()).contains(new URL(HOMEPAGE_URL));
            assertThat(meta.getAttribution()).contains(ATTRIBUTION);
            assertThat(meta.getDownloadLocation()).contains(URI.create(DOWNLOAD_URI));
            assertThat(meta.getSha1()).contains(SHA1);
            assertThat(meta.getSha256()).contains(SHA256);
            assertThat(meta.getSourceLocation()).contains(URI.create(SOURCE_URI));
            assertThat(meta.getDeclaredLicense()).contains(DECLARED_LICENSE);
            assertThat(meta.getDetectedLicense()).contains(DETECTED_LICENSE);
        }

        @Test
        void ignoresMalformedValue() {
            meta.attributes.put(TITLE, 123);

            assertThat(meta.getTitle()).isEmpty();
        }

        @Test
        void ignoresInvalidURL() {
            meta.attributes.put(HOMEPAGE, "ssh:/not/a/homepage");

            assertThat(meta.getHomePage()).isEmpty();
        }
    }
}
