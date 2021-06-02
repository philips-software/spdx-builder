/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.License;

import java.util.List;
import java.util.UUID;

interface BlackDuckComponent {
    String getName();

    UUID getId();

    String getVersion();

    UUID getVersionId();

    List<PackageURL> getPackageUrls();

    List<String> getUsages();

    License getLicense();

    long getHierarchicalId();

    boolean isSubproject();
}
