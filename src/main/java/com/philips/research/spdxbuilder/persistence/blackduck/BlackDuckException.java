/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
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
