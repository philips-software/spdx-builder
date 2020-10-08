/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.bom.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrtJsonTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String VALID_URL = "http://example.com";

    @Nested
    class PackageJsonTest {
        private static final String HASH_TYPE = "SHA-1";
        private static final String HASH_VALUE = "abc123";
        private final PackageJson pkg = new PackageJson();

        @BeforeEach
        void beforeEach() {
            pkg.id = String.join(":", List.of(TYPE, NAMESPACE, NAME, VERSION));
        }

        @Test
        void createsPackage() {
            final var result = pkg.createPackage();

            assertThat(result.getType()).isEqualTo(TYPE.toLowerCase());
            assertThat(result.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(result.getName()).isEqualTo(NAME);
            assertThat(result.getVersion()).isEqualTo(VERSION);
        }

        @Test
        void extractsSourceLocation() {
            pkg.sourceArtifact = new LocationJson();
            pkg.sourceArtifact.url = URI.create(VALID_URL);

            final var result = pkg.createPackage();

            assertThat(result.getLocation()).contains(URI.create(VALID_URL));
        }

        @Test
        void addsSourceHash() {
            var locationJson = new LocationJson();
            var hashJson = new HashJson();
            hashJson.algorithm = HASH_TYPE;
            hashJson.value = HASH_VALUE;
            locationJson.hash = hashJson;
            locationJson.url = URI.create(VALID_URL);
            pkg.sourceArtifact = locationJson;

            final var result = pkg.createPackage();

            assertThat(result.getLocation()).contains(URI.create(VALID_URL));
            assertThat(result.getHash(HASH_TYPE)).contains(HASH_VALUE);
        }
    }

    @Nested
    class VcsJsonTest {
        final Package result = new Package(TYPE, NAMESPACE, NAME, VERSION);

        @Test
        void noLocation_emptyUrlField() {
            final var json = new VcsJson();

            json.addSourceLocation(result);

            assertThat(result.getLocation()).isEmpty();
        }

        @Test
        void vcsLocationWithRevisionAndPath() {
            final var json = new VcsJson();
            json.type = "Git";
            json.url = URI.create(VALID_URL);
            json.path = "the?path";
            json.revision = "the?revision";

            json.addSourceLocation(result);

            assertThat(result.getLocation()).contains(URI.create("git+" + VALID_URL + "@the%3Frevision#the%3Fpath"));
        }

        @Test
        void vcsLocationWithProvidedVersion() {
            final var json = new VcsJson();
            json.url = URI.create(VALID_URL);

            json.addSourceLocation(result);

            assertThat(result.getLocation()).contains(URI.create(VALID_URL + "@" + VERSION));
        }
    }
}
