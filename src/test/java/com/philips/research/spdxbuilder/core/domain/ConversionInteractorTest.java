/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.philips.research.spdxbuilder.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ConversionInteractorTest {
    private static final File ORT_FILE = Path.of("src", "main", "resources", "ort_sample.yml").toFile();
    private static final String TYPE = "Type";
    private static final String GROUP = "Namespace";
    private static final String PROJECT = "Project";
    private static final String ORGANIZATION = "Organization";
    private static final String NAME = "Package";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";
    private static final URI LOCATION = URI.create("www.example.com");
    private static final String COMMENT = "Comment";
    private static final URI NAMESPACE_URI = URI.create("http://example.com");
    private static final URI PURL = URI.create("pkg:/group/name");

    private final BomReader reader = mock(BomReader.class);
    private final BomWriter writer = mock(BomWriter.class);
    private final KnowledgeBase knowledgeBase = mock(KnowledgeBase.class);
    private final BillOfMaterials bom = new BillOfMaterials();
    private final ConversionService interactor = new ConversionInteractor(reader, writer, bom)
            .setKnowledgeBase(knowledgeBase);
    private final Package project = new Package(GROUP, PROJECT, VERSION);
    private final Package pkg = new Package(GROUP, NAME, VERSION);

    @BeforeEach
    void beforeEach() {
        when(knowledgeBase.enhance(any(BillOfMaterials.class))).thenReturn(true);
    }

    @Test
    void convertsBillOfMaterials() {
        interactor.convert(false);

        verify(reader).read(bom);
        verify(knowledgeBase).enhance(bom);
        verify(writer).write(bom);
    }

    @Test
    void throws_enhancementFailure() {
        when(knowledgeBase.enhance(any(BillOfMaterials.class))).thenReturn(false);

        assertThatThrownBy(() -> interactor.convert(false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Enhancement of metadata failed");
    }

    @Test
    void continues_enhancementFailure() {
        when(knowledgeBase.enhance(any(BillOfMaterials.class))).thenReturn(false);

        interactor.convert(true);
    }

    @Test
    void skipsEnhancement_noKnowledgeBaseConfigured() {
        //noinspection ConstantConditions
        ((ConversionInteractor) interactor).setKnowledgeBase(null);

        interactor.convert(false);

        verify(knowledgeBase, never()).enhance(bom);
    }

    @Test
    void setsDocumentProperties() {
        interactor.setDocument(PROJECT, ORGANIZATION);
        interactor.setComment(COMMENT);

        assertThat(bom.getTitle()).isEqualTo(PROJECT);
        assertThat(bom.getOrganization().orElseThrow().getName()).isEqualTo(ORGANIZATION);
        assertThat(bom.getComment()).contains(COMMENT);
    }

    @Test
    void setsDocumentIdentification() {
        interactor.setDocNamespace(NAMESPACE_URI);
        interactor.setDocReference(PROJECT);

        assertThat(bom.getNamespace()).contains(NAMESPACE_URI);
        assertThat(bom.getIdentifier()).contains(PROJECT);
    }

//    @Nested
//    class Curation {
//        private final Package otherPkg = new Package(TYPE, GROUP, NAME, VERSION);
//
//        @BeforeEach
//        void setUp() {
//            pkg.setPurl(PURL);
//            bom.addPackage(pkg).addPackage(otherPkg);
//        }
//
//        @Test
//        void curatesPackageLicense() {
//            interactor.curatePackageLicense(PURL, LICENSE);
//
//            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
//            assertThat(otherPkg.getConcludedLicense()).isEmpty();
//        }
//
//        @Test
//        void curatesPackageSource() {
//            interactor.curatePackageSource(PURL, LOCATION);
//
//            assertThat(pkg.getSourceLocation()).contains(LOCATION);
//            assertThat(otherPkg.getSourceLocation()).isEmpty();
//        }
//    }
}
