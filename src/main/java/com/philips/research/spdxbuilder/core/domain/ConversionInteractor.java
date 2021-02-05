/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.KnowledgeBase;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of conversion use cases.
 */
public class ConversionInteractor implements ConversionService {
    private final BomReader reader;
    private final BomWriter writer;
    private final BillOfMaterials bom;

    private @NullOr KnowledgeBase knowledgeBase;

    public ConversionInteractor(BomReader reader, BomWriter writer) {
        this(reader, writer, new BillOfMaterials());
    }

    ConversionInteractor(BomReader reader, BomWriter writer, BillOfMaterials bom) {
        this.reader = reader;
        this.writer = writer;
        this.bom = bom;
    }

    public ConversionInteractor setKnowledgeBase(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        return this;
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
    public void curatePackageLicense(URI purl, String license) {
        //FIXME Should be stored first
        curate(purl, pkg -> pkg.setConcludedLicense(license));
    }

    @Override
    public void curatePackageSource(URI purl, URI source) {
        //FIXME Should be stored first
        curate(purl, pkg -> pkg.setSourceLocation(source));
    }

    @Override
    public void convert() {
        reader.read(bom);
        if (knowledgeBase != null) {
            knowledgeBase.enhance(bom);
        }
        //TODO Curate before writing
        writer.write(bom);
    }

    private void curate(URI purl, Consumer<Package> curate) {
        bom.getPackages().stream()
                .filter(pkg -> Objects.equals(purl, pkg.getPurl()))
                .forEach(curate);
    }
}
