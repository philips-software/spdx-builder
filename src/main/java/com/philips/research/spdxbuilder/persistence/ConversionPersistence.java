/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.ConversionStore;
import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;
import com.philips.research.spdxbuilder.persistence.license.LicenseScannerClient;
import com.philips.research.spdxbuilder.persistence.license.LicenseScannerException;
import com.philips.research.spdxbuilder.persistence.ort.OrtReader;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;
import java.util.Optional;

/**
 * Persistence implementation for bill-of-material data in various formats.
 */
public class ConversionPersistence implements ConversionStore {
    private final @NullOr LicenseScannerClient licenseClient;

    public ConversionPersistence(@NullOr URI licenseScannerUri) {
        licenseClient = (licenseScannerUri != null)
                ? new LicenseScannerClient(licenseScannerUri)
                : null;
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
    public Optional<LicenseInfo> detectLicense(Package pkg) {
        try {
            if (licenseClient == null) {
                return Optional.empty();
            }
            return licenseClient.scanLicense(pkg.getNamespace(), pkg.getName(), pkg.getVersion(), pkg.getLocation().orElse(null));
        } catch (LicenseScannerException e) {
            System.err.println("ERROR: " + e.getMessage());
            return Optional.empty();
        }
    }
}
