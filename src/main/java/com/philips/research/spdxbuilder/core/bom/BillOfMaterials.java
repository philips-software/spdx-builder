/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.bom;

import java.util.*;

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

    public BillOfMaterials addRelation(Package from, Package to, Relation.Type type) {
        relations.add(new Relation(from, to, type));
        return this;
    }

    public Collection<Relation> getRelations() {
        return relations;
    }
}

