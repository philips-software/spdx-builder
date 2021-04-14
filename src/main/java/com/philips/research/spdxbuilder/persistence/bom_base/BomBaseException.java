/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import com.philips.research.spdxbuilder.core.BusinessException;

public class BomBaseException extends BusinessException {
    public BomBaseException(String message) {
        super(message);
    }
}
