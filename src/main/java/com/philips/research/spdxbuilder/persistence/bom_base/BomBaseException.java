/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import com.philips.research.spdxbuilder.core.BusinessException;

public class BomBaseException extends BusinessException {
    public BomBaseException(String message) {
        super(message);
    }
}
