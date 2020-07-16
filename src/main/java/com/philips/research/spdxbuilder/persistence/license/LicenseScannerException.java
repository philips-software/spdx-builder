/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import com.philips.research.spdxbuilder.core.BusinessException;

public class LicenseScannerException extends BusinessException {
    public LicenseScannerException(String message) {
        super(message);
    }
}
