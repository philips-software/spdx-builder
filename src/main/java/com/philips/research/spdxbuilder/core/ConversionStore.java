package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;

public interface ConversionStore {
    enum FileType {ORT, SPDX};

    BillOfMaterials read(FileType type, File file);

    void write(BillOfMaterials bom, FileType type, File file);

    void detectLicense(Package pkg);
}

