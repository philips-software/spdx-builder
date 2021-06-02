/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import java.net.URL;
import java.util.Optional;

public interface BlackDuckComponentDetails {
    Optional<String> getDescription();

    Optional<URL> getHomepage();
}
