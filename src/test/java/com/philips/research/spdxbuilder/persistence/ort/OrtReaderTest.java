/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class OrtReaderTest {
    private static final File ORT_SAMPLE = Path.of("src", "test", "resources", "ort_sample.yml").toFile();

    private final BillOfMaterials bom = new BillOfMaterials();

    @Test
    void loadsOrtSample() {
        new OrtReader(ORT_SAMPLE)
                .defineProjectPackage("NPM::mime-types:2.1.18", URI.create("pkg:npm/mime-types@2.1.18"))
                .excludeScopes("NPM::mime-types:2.1.18", List.of("test*"))
                .read(bom);

        assertThat(bom.getPackages()).hasSize(1 + 2);
    }
}
