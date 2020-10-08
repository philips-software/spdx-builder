/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;

/**
 * File I/O API for a bill-of-materials.
 */
public interface BillOfMaterialsStore {
    /**
     * @return the bill-of-materials stored in a file
     */
    BillOfMaterials read(File file);

    /**
     * Writes the bill-of-material to a file.
     */
    void write(File file, BillOfMaterials bom);
}
