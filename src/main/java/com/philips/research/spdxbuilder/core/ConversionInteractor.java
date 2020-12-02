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

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of conversion use cases.
 */
public class ConversionInteractor implements ConversionService {
    private final ConversionStore store;
    private final BillOfMaterials bom;
    private final Map<String, @NullOr URI> projectPackages = new HashMap<>();
    private final Map<String, List<String>> projectExcludes = new HashMap<>();

    public ConversionInteractor(ConversionStore store) {
        this(store, new BillOfMaterials());
    }

    ConversionInteractor(ConversionStore store, BillOfMaterials bom) {
        this.store = store;
        this.bom = bom;
    }

    @Override
    public void setDocument(String title, String organization) {
        bom.setTitle(title);
        bom.setOrganization(organization);
    }

    @Override
    public void setComment(String comment) {
        bom.setComment(comment);
    }

    @Override
    public void setDocReference(String spdxId) {
        bom.setIdentifier(spdxId);
    }

    @Override
    public void setDocNamespace(URI namespace) {
        bom.setNamespace(namespace);
    }

    @Override
    public void defineProjectPackage(String id, @NullOr URI purl) {
        //noinspection ConstantConditions
        projectPackages.put(id, purl);
    }

    @Override
    public void excludeScopes(String id, List<String> excluded) {
        projectExcludes.put(id, excluded);
    }

    @Override
    public void readOrtAnalysis(File file) {
        store.read(bom, projectPackages, projectExcludes, ConversionStore.FileType.ORT, file);
    }

    @Override
    public void scanLicenses() {
        bom.getPackages().forEach(this::updateLicense);
    }

    private void updateLicense(Package pkg) {
        store.detectLicense(pkg)
                .ifPresent(l -> {
                    pkg.setDetectedLicense(l.getLicense());
                    if (l.isConfirmed()) {
                        pkg.setConcludedLicense(l.getLicense());
                    }
                });
    }

    @Override
    public void curatePackageLicense(URI purl, String license) {
        curate(purl, pkg -> pkg.setConcludedLicense(license));
    }

    @Override
    public void curatePackageSource(URI purl, URI source) {
        curate(purl, pkg -> pkg.setLocation(source));
    }

    private void curate(URI purl, Consumer<Package> curate) {
        bom.getPackages().stream()
                .filter(pkg -> Objects.equals(purl, pkg.getPurl()))
                .forEach(curate);
    }

    @Override
    public void writeBillOfMaterials(File file) {
        store.write(bom, ConversionStore.FileType.SPDX, file);
    }
}
