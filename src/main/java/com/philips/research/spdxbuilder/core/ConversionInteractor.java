/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;

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
        bom.getProjects().forEach(this::updateLicense);
        bom.getPackages().forEach(this::updateLicense);
    }

    private void updateLicense(Package pkg) {
        store.detectLicense(pkg)
                .ifPresent(l -> {
                    pkg.setDetectedLicense(l.getLicense());
                    if (l.isConfirmed()) {
                        pkg.setConcludedLicense(l.getLicense());
                    }
                });
    }

    @Override
    public void writeBillOfMaterials(File file) {
        store.write(bom, ConversionStore.FileType.SPDX, file);
    }
}
