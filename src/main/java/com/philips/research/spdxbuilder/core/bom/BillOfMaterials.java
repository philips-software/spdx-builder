/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core.bom;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.util.*;

/**
 * Report on the composition of a product
 */
public class BillOfMaterials {
    private final List<Package> packages = new ArrayList<>();
    private final Set<Relation> relations = new HashSet<>();
    private @NullOr String title;
    private @NullOr String comment;
    private @NullOr String organization;
    private @NullOr String identifier;
    private @NullOr URI namespace;

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

    public String getTitle() {
        if (title == null) {
            return packages.stream()
                    .findFirst().map(Package::getName)
                    .orElse("");
        }
        return title;
    }

    public BillOfMaterials setTitle(String title) {
        this.title = title;
        return this;
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    public BillOfMaterials setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Optional<String> getOrganization() {
        return Optional.ofNullable(organization);
    }

    public BillOfMaterials setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }

    public BillOfMaterials setIdentifier(@NullOr String identifier) {
        this.identifier = (identifier != null && !identifier.isBlank()) ? identifier : null;
        return this;
    }

    public Optional<URI> getNamespace() {
        return Optional.ofNullable(namespace);
    }

    public BillOfMaterials setNamespace(URI namespace) {
        this.namespace = namespace;
        return this;
    }
}

