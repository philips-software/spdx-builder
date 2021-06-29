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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OrtReaderTest {

    private static final Path SAMPLES_DIR = Path.of("src", "test", "resources");
    private static final File ORT_SAMPLE = SAMPLES_DIR.resolve("ort_sample.yml").toFile();
    private static final File ORT_SAMPLE_WITH_ISSUE = SAMPLES_DIR.resolve("ort_with_issue.yml").toFile();

    private final BillOfMaterials bom = new BillOfMaterials();

    void createBOM(File file) {
        OrtReader ortSample = new OrtReader(file);
        ortSample.defineProjectPackage("NPM::mime-types:2.1.18", URI.create("pkg:npm/mime-types@2.1.18"))
                .excludeScopes("NPM::mime-types:2.1.18", List.of("test*"))
                .read(bom);
    }

    @Test
    void loadsOrtSample() {
        createBOM(ORT_SAMPLE);
        assertThat(bom.getPackages()).hasSize(1 + 2);
    }

    @Test()
    void abortsOnAnalyzerIssues() {
        assertThatThrownBy(() -> createBOM(ORT_SAMPLE_WITH_ISSUE))
                .isInstanceOf(OrtReaderException.class)
                .hasMessageContaining("The analyzed ORT file has issues");
    }
}
