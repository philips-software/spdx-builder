/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.github.packageurl.PackageURL;

import java.net.URI;

/**
 * Conversion use cases.
 */
public interface ConversionService {
    /**
     * Configure general document properties.
     *
     * @param title
     * @param organization
     */
    void setDocument(String title, String organization);

    /**
     * Configure document comment.
     *
     * @param comment
     */
    void setComment(String comment);

    /**
     * Configure SPDX reference.
     *
     * @param spdxId Document identifier
     */
    void setDocReference(String spdxId);

    /**
     * Configure document namespace.
     *
     * @param namespace Namespace URL
     */
    void setDocNamespace(URI namespace);

    /**
     * Set alternative license for a package.
     *
     * @param purl    identification of the package
     * @param license curated license
     */
    void curatePackageLicense(PackageURL purl, String license);

    /**
     * Set alternative source for a package.
     *
     * @param purl   identification of the package
     * @param source source location
     */
    void curatePackageSource(PackageURL purl, URI source);

    /**
     * Reads a bill-of-materials, extends it with metadata from the knowledge base (if configured),
     * and writes it as a document.
     *
     * @param continueWhenIncomplete writes the SBOM even if the conversion is incomplete
     */
    void convert(boolean continueWhenIncomplete);
}
