/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;

public interface BillOfMaterialsStore {
    BillOfMaterials read(File file);

    void write(File file, BillOfMaterials bom);
}
