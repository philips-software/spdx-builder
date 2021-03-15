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

/**
 * Interface for reading a bill-of-materials
 */
public interface BomReader {
    void read(BillOfMaterials bom);
}
