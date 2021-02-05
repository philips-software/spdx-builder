/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import pl.tlinkowski.annotation.basic.NullOr;

class ResultJson {
    String id;
    @NullOr String license;
    boolean confirmed;
}
