/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class OrtReaderTest {
    private static final File ORT_SAMPLE = Path.of("src", "test", "resources", "ort_sample.yml").toFile();

    private final BillOfMaterials bom = new BillOfMaterials();

    @Test
    void loadsOrtSample() {
        new OrtReader().read(ORT_SAMPLE, bom, Map.of("NPM::mime-types:2.1.18", URI.create("pkg:npm/mime-types@2.1.18")), Map.of());

        assertThat(bom.getPackages()).hasSize(1 + 2);
    }
}
