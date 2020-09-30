/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import pl.tlinkowski.annotation.basic.NullOr;

import java.util.UUID;

class ResultJson {
    @NullOr UUID id;
    @NullOr String license;
    boolean confirmed;
}
