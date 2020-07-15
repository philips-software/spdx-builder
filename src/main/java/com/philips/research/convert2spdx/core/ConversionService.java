package com.philips.research.convert2spdx.core;

import java.io.File;

public interface ConversionService {
    /**
     * Reads the result of an OSS Review Toolkit analysis.
     *
     * @param file YAML file.
     */
    void readBillOfMaterials(File file);

    void writeBillOfMaterials(File file);
}
