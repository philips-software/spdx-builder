/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;

import java.io.File;
import java.util.Optional;

/**
 * Persistence API for bill-of-materials entities.
 */
public interface ConversionStore {
    /**
     * Reads a bill-of-materials for the indicated file type.
     */
    BillOfMaterials read(FileType type, File file);

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

