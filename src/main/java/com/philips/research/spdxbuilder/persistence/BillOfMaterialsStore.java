/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
