/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.license;

import com.philips.research.spdxbuilder.core.BusinessException;

/**
 * Exception thrown in case of an error while obtaining license information.
 */
public class LicenseScannerException extends BusinessException {
    public LicenseScannerException(String message) {
        super(message);
    }
}
