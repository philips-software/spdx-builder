/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistence API for bill-of-materials entities.
 */
public interface ConversionStore {
    /**
     * Reads a bill-of-materials for the indicated file type.
     */
    void read(BillOfMaterials bom, Map<String, URI> projectPackages, Map<String, List<String>> projectExcludes, FileType type, File file);

    /**
     * Write a bill-of-materials for the indicated file type.
     */
    void write(BillOfMaterials bom, FileType type, File file);

    /**
     * @return the scanned license for a package
     */
    Optional<LicenseInfo> detectLicense(Package pkg);

    enum FileType {ORT, SPDX}

    class LicenseInfo {
        private final String license;
        private final boolean confirmed;

        public LicenseInfo(String license, boolean confirmed) {
            this.license = license;
            this.confirmed = confirmed;
        }

        public String getLicense() {
            return license;
        }

        public boolean isConfirmed() {
            return confirmed;
        }
    }
}

