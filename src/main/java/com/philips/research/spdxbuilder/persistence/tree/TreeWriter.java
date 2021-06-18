/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.BomProcessor;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;

import java.util.*;

/**
 * Writes the packages of a bill-of-materials as a tree to the console.
 */
public class TreeWriter implements BomProcessor {
    private static final String SNIP = "-".repeat(10) + "8<" + "-".repeat(10);

    private final Map<Package, List<Relation>> nodes = new HashMap<>();
    private final List<Package> roots = new ArrayList<>();
    private final Set<Package> done = new HashSet<>();
    private final TreeFormatter formatter;

    public TreeWriter() {
        this(new TreeFormatter());
    }

    TreeWriter(TreeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void process(BillOfMaterials bom) {
        buildNodes(bom);

        System.out.println("TREE start " + SNIP);
        roots.forEach(pkg -> {
            System.out.println(formatter.node(name(pkg)));
            writeRelationsOf(pkg);
        });
        System.out.println("TREE end " + SNIP);
    }

    private void buildNodes(BillOfMaterials bom) {
        nodes.clear();
        bom.getPackages().forEach(pkg -> nodes.put(pkg, new ArrayList<>()));

        roots.clear();
        roots.addAll(nodes.keySet());
        roots.sort(Comparator.comparing(this::name));

        bom.getRelations().forEach(rel -> {
            roots.remove(rel.getTo());
            nodes.get(rel.getFrom()).add(rel);
        });
        nodes.values().forEach(list -> list.sort(Comparator.comparing(rel -> name(rel.getTo()))));

        done.clear();
        done.addAll(roots);
    }

    private void writeRelationsOf(Package pkg) {
        done.add(pkg);
        final var relations = nodes.get(pkg);
        if (!relations.isEmpty()) {
            formatter.indent();
            relations.forEach(this::writeRelation);
            formatter.unindent();
        }
    }

    private void writeRelation(Relation relation) {
        Package pkg = relation.getTo();
        final var name = name(pkg);
        final var type = type(relation);
        if (!done.contains(pkg)) {
            System.out.println(formatter.node(name + type));
            writeRelationsOf(pkg);
        } else {
            final var omitted = nodes.get(pkg).isEmpty() ? "" : " (*)";
            System.out.println(formatter.node(name + type + omitted));
        }
    }

    private String type(Relation relation) {
        switch (relation.getType()) {
            case DESCENDANT_OF:
                return " [derived]";
            case DYNAMICALLY_LINKS:
                return " [dynamic]";
            case STATICALLY_LINKS:
                return " [static]";
            case CONTAINS:
                return " [contained]";
            case DEVELOPED_USING:
                return " [dev]";
            case DEPENDS_ON:
                return "";
            default:
                System.err.println("WARNING: Unmapped tree relation type:" + relation.getType());
                return "";
        }
    }

    private String name(Package pkg) {
        return pkg.getPurl().orElseGet(() -> {
            try {
                return new PackageURL("generic", pkg.getNamespace(), pkg.getName(), pkg.getVersion(), null, null);
            } catch (MalformedPackageURLException e) {
                throw new IllegalArgumentException("Failed to create generic package URL for " + pkg);
            }
        }).toString();
    }
}
