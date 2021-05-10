/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.domain.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrtJsonTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String FILENAME = "file.name";
    private static final String VALID_URL = "http://example.com/path/to/" + FILENAME;

    @Nested
    class PackageJsonTest {
        private static final String HASH_VALUE = "abc123";
        private final PackageJson pkg = new PackageJson();

        @BeforeEach
        void beforeEach() {
            pkg.id = String.join(":", List.of(TYPE, NAMESPACE, NAME, VERSION));
        }

        @Test
        void createsPackage() {
            final var result = pkg.createPackage();

            assertThat(result.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(result.getName()).isEqualTo(NAME);
            assertThat(result.getVersion()).isEqualTo(VERSION);
        }

        @Test
        void throws_invalidPackageUrlFormat() {
            pkg.purl = URI.create("Not_a_valid_PURL");

            assertThatThrownBy(pkg::createPackage)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not a valid Package URL");
        }

        @Test
        void extractsSourceLocation() {
            pkg.sourceArtifact = new LocationJson();
            pkg.sourceArtifact.url = URI.create(VALID_URL);

            final var result = pkg.createPackage();

            assertThat(result.getSourceLocation()).contains(URI.create(VALID_URL));
        }

        @Test
        void addsBinaryFileHash() {
            var locationJson = new LocationJson();
            var hashJson = new HashJson();
            hashJson.algorithm = "SHA-1";
            hashJson.value = HASH_VALUE;
            locationJson.hash = hashJson;
            locationJson.url = URI.create(VALID_URL);
            pkg.binaryArtifact = locationJson;

            final var result = pkg.createPackage();

            assertThat(result.getFilename()).contains(FILENAME);
            assertThat(result.getHashes()).containsEntry("SHA1", HASH_VALUE);
        }
    }

    @Nested
    class VcsJsonTest {
        final Package result = new Package(NAMESPACE, NAME, VERSION);

        @Test
        void noLocation_emptyUrlField() {
            final var json = new VcsJson();

            json.addSourceLocation(result);

            assertThat(result.getSourceLocation()).isEmpty();
        }

        @Test
        void vcsLocationWithRevisionAndPath() {
            final var json = new VcsJson();
            json.type = "Git";
            json.url = VALID_URL;
            json.path = "the?path";
            json.revision = "the?revision";

            json.addSourceLocation(result);

            assertThat(result.getSourceLocation()).contains(URI.create("git+" + VALID_URL + "@the%3Frevision#the%3Fpath"));
        }

        @Test
        void vcsLocationWithProvidedVersion() {
            final var json = new VcsJson();
            json.url = VALID_URL;

            json.addSourceLocation(result);

            assertThat(result.getSourceLocation()).contains(URI.create(VALID_URL + "@" + VERSION));
        }
    }
}
