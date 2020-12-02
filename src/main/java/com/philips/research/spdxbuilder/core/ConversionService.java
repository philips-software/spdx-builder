/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core;

import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;
import java.util.List;

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
     * Marks a project for import with an (optional) package alias
     *
     * @param id   ORT identifier for the project
     * @param purl (Optional) Package URL
     */
    void defineProjectPackage(String id, @NullOr URI purl);

    /**
     * Adds excluded scopes to ORT.
     *
     * @param id       ORT identifier for the project
     * @param excluded (optionally wild-carded) scope names
     */
    void excludeScopes(String id, List<String> excluded);

    /**
     * Reads the result of an OSS Review Toolkit analysis.
     *
     * @param file YAML file
     */
    void readOrtAnalysis(File file);

    /**
     * Scans licenses for all bill-of-material items.
     */
    void scanLicenses();

    /**
     * Set alternative license for a package.
     *
     * @param purl    identification of the package
     * @param license curated license
     */
    void curatePackageLicense(URI purl, String license);

    /**
     * Set alternative source for a package.
     *
     * @param purl   identification of the package
     * @param source source location
     */
    void curatePackageSource(URI purl, URI source);

    /**
     * Writes an SPDX bill-of-materials.
     *
     * @param file output file
     */
    void writeBillOfMaterials(File file);

}
