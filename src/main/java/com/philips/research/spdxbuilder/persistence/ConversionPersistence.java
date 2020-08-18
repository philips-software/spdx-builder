/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.ConversionStore;
import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.persistence.license.LicenseScannerClient;
import com.philips.research.spdxbuilder.persistence.ort.OrtReader;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;

import java.io.File;
import java.net.URI;
import java.util.Optional;

/**
 * Persistence implementation for bill-of-material data in various formats.
 */
public class ConversionPersistence implements ConversionStore {
    private final LicenseScannerClient licenseClient;

    public ConversionPersistence(URI licenseScannerUri) {
        licenseClient = new LicenseScannerClient(licenseScannerUri);
    }

    @Override
    public BillOfMaterials read(FileType type, File file) {
        //TODO switch on file type
        return new OrtReader().read(file);
    }

    @Override
    public void write(BillOfMaterials bom, FileType type, File file) {
        //TODO switch on file type
        new SpdxWriter().write(file, bom);
    }

    @Override
    public Optional<String> detectLicense(String namespace, String name, String version, URI location) {
        return licenseClient.scanLicense(namespace, name, version, location);
    }
}
