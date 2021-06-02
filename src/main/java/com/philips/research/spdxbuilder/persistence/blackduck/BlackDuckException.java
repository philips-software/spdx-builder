/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

public class BlackDuckException extends RuntimeException {
    public BlackDuckException(String message) {
        super(message);
    }

    public BlackDuckException(String message, Throwable cause) {
        super(message, cause);
    }
}
