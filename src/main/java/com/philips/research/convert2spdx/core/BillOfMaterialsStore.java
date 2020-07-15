package com.philips.research.convert2spdx.core;

import com.philips.research.convert2spdx.core.bom.BillOfMaterials;

import java.io.File;

public interface BillOfMaterialsStore {
    BillOfMaterials read(File file);

    void write(File file, BillOfMaterials bom);
}
