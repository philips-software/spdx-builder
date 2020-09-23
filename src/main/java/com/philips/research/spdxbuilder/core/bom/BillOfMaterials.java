/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import com.philips.research.spdxbuilder.core.ConversionStore;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

/**
 * Report on the composition of a product
 */
public class BillOfMaterials {
    private final List<Package> projects = new ArrayList<>();
    private final List<Package> packages = new ArrayList<>();
    private final Set<Relation> relations = new HashSet<>();

    public List<Package> getProjects() {
        return projects;
    }

    public BillOfMaterials addProject(Package product) {
        projects.add(product);
        return this;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public BillOfMaterials addPackage(Package pkg) {
        packages.add(pkg);
        return this;
    }

    public void updateLicense(QueryLicense func) {
//        projects.forEach(updatePackageLicense(func));
        packages.forEach(updatePackageLicense(func));
    }

    private Consumer<Package> updatePackageLicense(QueryLicense func) {
        return p -> func.query(p.getNamespace(), p.getName(), p.getVersion(), p.getLocation().orElse(null))
                .ifPresent(info -> {
                    p.setDetectedLicense(info.getLicense());
                    if (info.isConfirmed()) {
                        p.setConcludedLicense(info.getLicense());
                    }
                });
    }

    public BillOfMaterials addRelation(Package from, Package to, Relation.Type type) {
        relations.add(new Relation(from, to, type));
        return this;
    }

    public Collection<Relation> getRelations() {
        return relations;
    }

    @FunctionalInterface
    public interface QueryLicense {
        Optional<ConversionStore.LicenseInfo> query(String namespace, String name, String version, @NullOr URI location);
    }
}

