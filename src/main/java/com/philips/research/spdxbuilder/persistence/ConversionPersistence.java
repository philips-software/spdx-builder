package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.ConversionStore;
import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.persistence.ort.OrtReader;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;

import java.io.File;

public class ConversionPersistence implements ConversionStore {
    @Override
    public BillOfMaterials read(FileType type, File file) {
        //TODO switch on file type
        return new OrtReader().read(file);
    }

    @Override
    public void write(BillOfMaterials bom, FileType type, File file) {
        //TODO switch on file type
        new SpdxWriter().write(file, bom);
    }

    @Override
    public void detectLicense(Package pkg) {
    }
}
