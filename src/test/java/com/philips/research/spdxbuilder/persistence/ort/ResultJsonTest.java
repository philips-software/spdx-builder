/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultJsonTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";

    @Nested
    class PackageJsonTest {
        private final PackageJson pkg = new PackageJson();

        @BeforeEach
        void beforeEach() {
            pkg.id = String.join(":", List.of(TYPE, NAMESPACE, NAME, VERSION));
        }

        @Test
        void extractsIdComponents() {
            assertThat(pkg.getType()).isEqualTo(TYPE);
            assertThat(pkg.getNamespace()).isEqualTo(NAMESPACE);
            assertThat(pkg.getName()).isEqualTo(NAME);
            assertThat(pkg.getVersion()).isEqualTo(VERSION);
        }
    }
}
