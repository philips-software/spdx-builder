/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * File I/O API for a bill-of-materials.
 */
public interface BillOfMaterialsStore {
    /**
     * @return the bill-of-materials stored in a file
     */
    void read(File file, BillOfMaterials bom, Map<String, URI> projectPackages, Map<String, List<String>> projectExcludes);

    /**
     * Writes the bill-of-material to a file.
     */
    void write(File file, BillOfMaterials bom);
}
