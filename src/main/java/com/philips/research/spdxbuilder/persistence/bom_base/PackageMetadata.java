/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
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

    Optional<URI> getDownloadLocation();

    Optional<String> getSha1();

    Optional<String> getSha256();

    Optional<URI> getSourceLocation();

    Optional<String> getDeclaredLicense();

    Optional<String> getDetectedLicense();
}
