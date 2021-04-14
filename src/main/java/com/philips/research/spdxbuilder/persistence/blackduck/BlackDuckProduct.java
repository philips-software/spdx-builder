/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import java.util.Optional;
import java.util.UUID;

public interface BlackDuckProduct {
    UUID getId();

    String getName();

    Optional<String> getDescription();
}
