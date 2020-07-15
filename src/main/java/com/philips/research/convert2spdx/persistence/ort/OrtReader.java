package com.philips.research.convert2spdx.persistence.ort;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.philips.research.convert2spdx.core.BillOfMaterialsStore;
import com.philips.research.convert2spdx.core.bom.BillOfMaterials;
import com.philips.research.convert2spdx.core.bom.Package;

import java.io.File;
import java.io.IOException;

public class OrtReader implements BillOfMaterialsStore {
    public BillOfMaterials read(File file) {
        try {
            final var yaml = new YAMLMapper().readValue(file, OrtJson.class);
            final var product = new Package(yaml.analyzer.result.projects.get(0).id, "version");
            final var bom = new BillOfMaterials(product);
            return bom;
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }

    public void write(File file, BillOfMaterials bom) {
        // Not implemented
    }
}
