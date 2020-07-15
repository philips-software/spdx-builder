package com.philips.research.convert2spdx.core;

import com.philips.research.convert2spdx.core.bom.BillOfMaterials;
import com.philips.research.convert2spdx.core.bom.Package;

import java.io.File;

public class ConversionInteractor implements ConversionService {
    private final BillOfMaterials bom = new BillOfMaterials(new Package("(None)", ""));

    public void readBillOfMaterials(File file) {

    }

    public void writeBillOfMaterials(File file) {

    }
}
