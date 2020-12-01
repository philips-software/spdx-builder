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

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
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
        new OrtReader().read(ORT_SAMPLE, bom, Map.of("NPM::mime-types:2.1.18", URI.create("pkg:npm/mime-types@2.1.18")));

        assertThat(bom.getPackages()).hasSize(1 + 2);
    }
}
