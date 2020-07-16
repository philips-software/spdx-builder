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
     * Writes an SPDX bill-of-materials.
     *
     * @param file output file
     */
    void writeBillOfMaterials(File file);
}
