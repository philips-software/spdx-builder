/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.BusinessException;

public class OrtReaderException extends BusinessException {
    public OrtReaderException(String message) {
        super(message);
    }
}
