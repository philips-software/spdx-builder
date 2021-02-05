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
import com.philips.research.spdxbuilder.core.domain.Package;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

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
                    final var declared = pkg.getDeclaredLicense().orElse(l.getLicense());
                    pkg.setDetectedLicense(l.getLicense());
                    if (l.isConfirmed()) {
                        pkg.setConcludedLicense(l.getLicense());
                    } else if (!Objects.equals(l.getLicense(), declared)) {
                        licenseClient.contest(pkg.getPurl(), declared);
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
