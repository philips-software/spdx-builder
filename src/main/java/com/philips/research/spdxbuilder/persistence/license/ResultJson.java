/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;

class ResultJson {
    @NullOr String namespace;
    @NullOr String name;
    @NullOr String version;
    @NullOr URI location;
    @NullOr String license;
    boolean confirmed;
}
