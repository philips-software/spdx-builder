/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;

/**
 * Interface for persisting a bill-of-materials.
 */
public interface BomProcessor {
    void process(BillOfMaterials bom);
}
