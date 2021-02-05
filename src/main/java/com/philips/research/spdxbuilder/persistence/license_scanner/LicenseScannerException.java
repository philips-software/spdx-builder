/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.philips.research.spdxbuilder.core.BusinessException;

/**
 * Exception thrown in case of an error while obtaining license information.
 */
public class LicenseScannerException extends BusinessException {
    public LicenseScannerException(String message) {
        super(message);
    }
}
