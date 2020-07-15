package com.philips.research.convert2spdx.persistence.spdx;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpdxRefTest {
    private static final String VALUE = "Value";

    @Test
    void createsInstance() {
        final var ref = new SpdxRef(VALUE);

        assertThat(ref.toString()).isEqualTo("SPDXRef-" + VALUE);
    }
}
