package com.philips.research.spdxbuilder.persistence.spdx;

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
