/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.spdx;

/**
 * Generic SPDX reference.
 */
public class SpdxRef {
    private final String ref;

    public SpdxRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return "SPDXRef-" + ref;
    }
}
