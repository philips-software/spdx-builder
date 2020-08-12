/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Report on the composition of a product
 */
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

    public void updateLicenses(QueryLicenses func) {
        projects.forEach(updatePackageLicenses(func));
        dependencies.forEach(updatePackageLicenses(func));
    }

    private Consumer<Package> updatePackageLicenses(QueryLicenses func) {
        return p -> func.query(p.getNamespace(), p.getName(), p.getVersion(), p.getLocation().orElse(null))
                .forEach(p::addDetectedLicense);
    }

    @FunctionalInterface
    public interface QueryLicenses {
        List<String> query(String namespace, String name, String version, URI location);
    }
}

