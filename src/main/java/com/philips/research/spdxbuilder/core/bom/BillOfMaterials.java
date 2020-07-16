package com.philips.research.spdxbuilder.core.bom;

import java.util.ArrayList;
import java.util.List;

public class BillOfMaterials {
    private final List<Package> projects = new ArrayList<>();
    private final List<Package> dependencies = new ArrayList<>();

    public List<Package> getProjects() {
        return projects;
    }

    public BillOfMaterials addProject(Package product) {
        projects.add(product);
        return this;
    }

    public List<Package> getDependencies() {
        return dependencies;
    }

    public BillOfMaterials addDependency(Package dependency) {
        dependencies.add(dependency);
        return this;
    }
}
