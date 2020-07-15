package com.philips.research.convert2spdx.persistence.ort;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class OrtReaderTest {
    private static final File ORT_SAMPLE = Path.of("src", "test", "resources", "ort_sample.yml").toFile();

    @Test
    void loadsOrtSample() {
//        final var bom = new OrtReader().read(ORT_SAMPLE);
//
//        final var product = bom.getProduct();
//        assertThat(product.getName()).isEqualTo("NPM::mime-types:2.1.18");
//        assertThat(product.getDeclaredLicense()).isEqualTo("MIT");
    }
}
