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

import com.github.packageurl.PackageURL;

import java.util.List;
import java.util.UUID;

interface BlackDuckComponent {
    String getName();

    UUID getId();

    String getVersion();

    UUID getVersionId();

    List<PackageURL> getPackageUrls();

    List<String> getUsages();

    long getHierarchicalId();
}
