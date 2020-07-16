package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;

public class ConversionInteractor implements ConversionService {
    private final ConversionStore store;

    private BillOfMaterials bom = new BillOfMaterials();

    public ConversionInteractor(ConversionStore store) {
        this.store = store;
    }

    public void readOrtAnalysis(File file) {
        bom = store.read(ConversionStore.FileType.ORT, file);
    }

    public void writeBillOfMaterials(File file) {
        store.write(bom, ConversionStore.FileType.SPDX, file);
    }
}
