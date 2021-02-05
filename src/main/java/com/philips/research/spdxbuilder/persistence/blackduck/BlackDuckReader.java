/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;

import java.net.URL;

public class BlackDuckReader implements BomReader {
    final BlackDuckClient client;
    private final String token;
    private final String project;
    private final String version;

    public BlackDuckReader(URL url, String token, String project, String version) {
        client = new BlackDuckClient(url);
        this.token = token;
        this.project = project;
        this.version = version;
    }

    @Override
    public void read(BillOfMaterials bom) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
