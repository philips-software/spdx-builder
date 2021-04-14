/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.philips.research.spdxbuilder.core.BusinessException;

public class OrtReaderException extends BusinessException {
    public OrtReaderException(String message) {
        super(message);
    }
}
