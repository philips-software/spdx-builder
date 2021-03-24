/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class KnowledgeBase {
    /**
     * Enhances all packages of a bill-of-materials.
     *
     * @param bom bill-of-materials
     * @return true if no packages failed
     */
    public boolean enhance(BillOfMaterials bom) {
        final var success = new AtomicBoolean(true);
        bom.getPackages().stream()
                .filter(pkg -> !pkg.isInternal())
                .forEach(pkg -> {
                    final var found = enhance(pkg);
                    if (!found) {
                        System.err.println("WARNING: No metadata for " + pkg);
                        success.set(false);
                    }
                });
        return success.get();
    }

    /**
     * Enhances a single package.
     *
     * @param pkg the package to enhance
     * @return true if for success, or false if enhancement failed
     */
    public abstract boolean enhance(Package pkg);
}
