/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license;

import java.net.URL;
import java.util.List;

class ResultJson {
    String namespace;
    String name;
    String version;
    URL location;
    List<String> licenses;
}
