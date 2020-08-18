/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;

/**
 * Implementation of the conversion use cases.
 */
public class ConversionInteractor implements ConversionService {
    private final ConversionStore store;

    private BillOfMaterials bom = new BillOfMaterials();

    public ConversionInteractor(ConversionStore store) {
        this.store = store;
    }

    @Override
    public void readOrtAnalysis(File file) {
        bom = store.read(ConversionStore.FileType.ORT, file);
    }

    @Override
    public void scanLicenses() {
        bom.updateLicense(store::detectLicense);
    }

    @Override
    public void writeBillOfMaterials(File file) {
        store.write(bom, ConversionStore.FileType.SPDX, file);
    }
}
