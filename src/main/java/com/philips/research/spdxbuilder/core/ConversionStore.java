/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;

import java.io.File;
import java.net.URI;
import java.util.List;

public interface ConversionStore {
    BillOfMaterials read(FileType type, File file);

    void write(BillOfMaterials bom, FileType type, File file);

    List<String> detectLicenses(String namespace, String name, String version, URI location);

    enum FileType {ORT, SPDX}
}

