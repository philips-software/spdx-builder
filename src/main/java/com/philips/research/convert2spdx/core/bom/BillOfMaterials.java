package com.philips.research.convert2spdx.core.bom;

import java.util.ArrayList;
import java.util.List;

public class BillOfMaterials {
    private final Package product;
    private final List<Package> dependencies = new ArrayList<>();

    public BillOfMaterials(Package product) {
        this.product = product;
    }

    public Package getProduct() {
        return product;
    }

    public List<Package> getDependencies() {
        return dependencies;
    }

    public BillOfMaterials addDependency(Package pkg) {
        dependencies.add(pkg);
        return this;
    }
}
