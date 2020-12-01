/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.BusinessException;

public class SpdxException extends BusinessException {
    public SpdxException(String message) {
        super(message);
    }
}
