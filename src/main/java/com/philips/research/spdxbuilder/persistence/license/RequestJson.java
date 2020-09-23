/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;

class RequestJson {
    @NullOr String location;

    public RequestJson(@NullOr URI location) {
        if (location != null) {
            this.location = location.toASCIIString();
        }
    }
}
