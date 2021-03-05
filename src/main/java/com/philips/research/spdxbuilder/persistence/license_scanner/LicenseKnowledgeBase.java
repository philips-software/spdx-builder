/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.philips.research.spdxbuilder.core.KnowledgeBase;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.LicenseParser;
import com.philips.research.spdxbuilder.core.domain.Package;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Knowledge base implementation for the License Scanner service.
 * See https://github.com/philips-software/license-scanner
 */
public class LicenseKnowledgeBase implements KnowledgeBase {
    final LicenseScannerClient licenseClient;

    public LicenseKnowledgeBase(URI uri) {
        this(new LicenseScannerClient(uri));
    }

    LicenseKnowledgeBase(LicenseScannerClient client) {
        this.licenseClient = client;
    }

    @Override
    public void enhance(BillOfMaterials bom) {
        bom.getPackages().forEach(this::updateLicense);
    }

    private void updateLicense(Package pkg) {
        detectLicense(pkg)
                .ifPresent(l -> {
                    final var scanned = LicenseParser.parse(l.getLicense());
                    final var declared = pkg.getDeclaredLicense().orElse(scanned);
                    pkg.setDetectedLicense(scanned);
                    if (l.isConfirmed()) {
                        pkg.setConcludedLicense(scanned);
                    } else if (!Objects.equals(scanned, declared)) { //FIXME should expand through dictionary
                        licenseClient.contest(pkg.getPurl(), declared.toString()); //FIXME should expand through dictionary
                    }
                });
    }

    private Optional<LicenseScannerClient.LicenseInfo> detectLicense(Package pkg) {
        try {
            return licenseClient.scanLicense(pkg.getPurl(), pkg.getSourceLocation().orElse(null));
        } catch (LicenseScannerException e) {
            System.err.println("ERROR: " + e.getMessage());
            return Optional.empty();
        }
    }
}
