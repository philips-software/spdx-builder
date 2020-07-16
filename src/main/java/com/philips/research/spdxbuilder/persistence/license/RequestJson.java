/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import java.net.URI;

class RequestJson {
    URI location;

    public RequestJson(URI location) {
        this.location = location;
    }
}
