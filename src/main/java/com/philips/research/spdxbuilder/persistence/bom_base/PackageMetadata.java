/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

public interface PackageMetadata {
    Optional<String> getTitle();

    Optional<String> getDescription();

    Optional<URL> getHomePage();

    Optional<String> getAttribution();

    Optional<String> getSupplier();

    Optional<String> getOriginator();

    Optional<URI> getDownloadLocation();

    Optional<String> getSha1();

    Optional<String> getSha256();

    Optional<URI> getSourceLocation();

    Optional<String> getDeclaredLicense();

    Optional<String> getDetectedLicense();
}
