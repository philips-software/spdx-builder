/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

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
