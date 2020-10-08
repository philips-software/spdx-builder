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

import java.io.File;

/**
 * Conversion use cases.
 */
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
