package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;

public interface BillOfMaterialsStore {
    BillOfMaterials read(File file);

    void write(File file, BillOfMaterials bom);
}
