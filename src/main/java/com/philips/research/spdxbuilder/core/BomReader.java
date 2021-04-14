/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;

/**
 * Interface for reading a bill-of-materials
 */
public interface BomReader {
    void read(BillOfMaterials bom);
}
