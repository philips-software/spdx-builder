/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import java.io.File;

public interface ConversionService {
    /**
     * Reads the result of an OSS Review Toolkit analysis.
     *
     * @param file YAML file
     */
    void readOrtAnalysis(File file);

    /**
     * Scans licenses for all bill-of-material items.
     */
    void scanLicenses();

    /**
     * Writes an SPDX bill-of-materials.
     *
     * @param file output file
     */
    void writeBillOfMaterials(File file);
}
