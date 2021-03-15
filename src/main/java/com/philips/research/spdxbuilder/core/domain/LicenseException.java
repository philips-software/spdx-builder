/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.philips.research.spdxbuilder.core.BusinessException;

public class LicenseException extends BusinessException {
    public LicenseException(String message) {
        super(message);
    }
}
