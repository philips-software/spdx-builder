/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import com.philips.research.spdxbuilder.core.KnowledgeBase;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.LicenseParser;

import java.net.URI;

public class BomBaseKnowledgeBase implements KnowledgeBase {
    private final BomBaseClient client;

    public BomBaseKnowledgeBase(URI serverUri) {
        this(new BomBaseClient(serverUri));
    }

    public BomBaseKnowledgeBase(BomBaseClient client) {
        this.client = client;
    }

    @Override
    public void enhance(BillOfMaterials bom) {
        bom.getPackages().forEach(pkg -> {
            client.readPackage(pkg.getPurl())
                    .ifPresent(meta -> {
                        meta.getTitle().ifPresent(pkg::setSummary);
                        meta.getDescription().ifPresent(pkg::setDescription);
                        meta.getHomePage().ifPresent(pkg::setHomePage);
                        meta.getSourceLocation().ifPresent(pkg::setSourceLocation);
                        meta.getSha1().ifPresent(hash -> pkg.addHash("SHA1", hash));
                        meta.getSha256().ifPresent(hash -> pkg.addHash("SHA256", hash));
                        meta.getDeclaredLicense().map(LicenseParser::parse).ifPresent(pkg::setDeclaredLicense);
                        meta.getDetectedLicense().map(LicenseParser::parse).ifPresent(pkg::setDetectedLicense);
                    });
        });
    }
}
