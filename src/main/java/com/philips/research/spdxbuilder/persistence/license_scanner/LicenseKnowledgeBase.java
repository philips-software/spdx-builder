/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.philips.research.spdxbuilder.core.KnowledgeBase;
import com.philips.research.spdxbuilder.core.domain.LicenseDictionary;
import com.philips.research.spdxbuilder.core.domain.LicenseParser;
import com.philips.research.spdxbuilder.core.domain.Package;

import java.net.URI;
import java.util.Optional;

/**
 * Knowledge base implementation for the License Scanner service.
 * See https://github.com/philips-software/license-scanner
 */
public class LicenseKnowledgeBase extends KnowledgeBase {
    final LicenseScannerClient licenseClient;

    public LicenseKnowledgeBase(URI uri) {
        this(new LicenseScannerClient(uri));
    }

    LicenseKnowledgeBase(LicenseScannerClient client) {
        this.licenseClient = client;
    }

    @Override
    public boolean enhance(Package pkg) {
        return detectLicense(pkg)
                .map(l -> {
                    final var scanned = LicenseParser.parse(l.getLicense());
                    final var declared = pkg.getDeclaredLicense().orElse(scanned);
                    pkg.setDetectedLicense(scanned);
                    if (l.isConfirmed()) {
                        pkg.setConcludedLicense(scanned);
                    } else {
                        final var dictionary = LicenseDictionary.getInstance();
                        final var scannedText = dictionary.expand(scanned);
                        final var declaredText = dictionary.expand(declared);
                        if (!scannedText.equals(declaredText)) {
                            licenseClient.contest(pkg.getPurl(), declaredText);
                        }
                    }
                    return l;
                }).isPresent();
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
