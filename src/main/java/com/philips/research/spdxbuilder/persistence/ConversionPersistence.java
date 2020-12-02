/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
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
import java.util.List;
import java.util.Map;
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

    ConversionPersistence(LicenseScannerClient client) {
        licenseClient = client;
    }

    @Override
    public void read(BillOfMaterials bom, Map<String, URI> projectPackages, Map<String, List<String>> projectExcludes, FileType type, File file) {
        //TODO switch on file type
        new OrtReader().read(file, bom, projectPackages, projectExcludes);
    }

    @Override
    public void write(BillOfMaterials bom, FileType type, File file) {
        //TODO switch on file type
        new SpdxWriter().write(file, bom);
    }

    @Override
    public Optional<LicenseInfo> detectLicense(Package pkg) {
        try {
            return (licenseClient != null) ? licenseClient.scanLicense(pkg) : Optional.empty();
        } catch (LicenseScannerException e) {
            System.err.println("ERROR: " + e.getMessage());
            return Optional.empty();
        }
    }
}
