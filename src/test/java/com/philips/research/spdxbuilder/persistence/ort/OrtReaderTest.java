package com.philips.research.spdxbuilder.persistence.ort;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class OrtReaderTest {
    private static final File ORT_SAMPLE = Path.of("src", "test", "resources", "ort_sample.yml").toFile();

    @Test
    void loadsOrtSample() {
        final var bom = new OrtReader().read(ORT_SAMPLE);

    }
}
