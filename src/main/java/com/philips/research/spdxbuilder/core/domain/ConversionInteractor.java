/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.*;
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
        bom.setOrganization(new Party(Party.Type.ORGANIZATION, organization));
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
    public void curatePackageLicense(PackageURL purl, String license) {
        //FIXME Should be stored first
        curate(purl, pkg -> pkg.setConcludedLicense(LicenseParser.parse(license)));
    }

    @Override
    public void curatePackageSource(PackageURL purl, URI source) {
        //FIXME Should be stored first
        curate(purl, pkg -> pkg.setSourceLocation(source));
    }

    @Override
    public void convert(boolean continueIfIncomplete) {
        reader.read(bom);
        if (knowledgeBase != null) {
            final var success = knowledgeBase.enhance(bom);
            if (!success && !continueIfIncomplete) {
                throw new BusinessException("Enhancement of metadata failed");
            }
        }
        //TODO Curate before writing
        writer.write(bom);
    }

    private void curate(PackageURL purl, Consumer<Package> curate) {
        bom.getPackages().stream()
                .filter(pkg -> Objects.equals(purl, pkg.getPurl()))
                .forEach(curate);
    }
}
