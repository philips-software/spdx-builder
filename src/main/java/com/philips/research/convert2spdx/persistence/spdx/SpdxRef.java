package com.philips.research.convert2spdx.persistence.spdx;

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
