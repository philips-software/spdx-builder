/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import java.net.URI;

class ResultJson {
    String namespace;
    String name;
    String version;
    URI location;
    String license;
    boolean confirmed;
}
