package com.philips.research.ort2spdx.persistence;

import com.philips.research.ort2spdx.core.BillOfMaterialsStore;
import com.philips.research.ort2spdx.core.bom.BillOfMaterials;

import java.io.File;

public class OrtReader implements BillOfMaterialsStore {
    public BillOfMaterials read(File file) {
        return null;
    }

    public void write(File file, BillOfMaterials bom) {
        // Not implemented
    }
}
