/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.philips.research.spdxbuilder.core.*;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final BomProcessor writer = mock(BomProcessor.class);
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
    void readsBillOfMaterials() {
        interactor.read();

        verify(reader).read(bom);
    }

    @Test
    void appliesBomProcessor() {
        interactor.apply(writer);

        verify(writer).process(bom);
    }

    @Test
    void convertsBillOfMaterials() {
        interactor.convert(false);

        verify(knowledgeBase).enhance(bom);
        verify(writer).process(bom);
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

    @Nested
    class spdxDocument {
        ByteArrayOutputStream spdxOutputStream = new ByteArrayOutputStream();
        Stream<String> lines;
        DateTimeFormatter isoFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneId.of("UTC"));
        String dateString = "2000-01-01T01:01:01.111Z";
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, parser);

        @BeforeEach
        void setUp() {
            bom.setCreatedAt(localDateTime);
            bom.addPackage(project).addPackage(pkg);
            BomProcessor spdxWriter = new SpdxWriter(spdxOutputStream);
            interactor.apply(spdxWriter);
            lines = spdxOutputStream.toString(Charset.defaultCharset()).lines();
        }

        @AfterEach
        void shutdown() throws IOException {
            spdxOutputStream.close();
        }

        @Test
        void verifyPackageWithoutSupplierField() {
            String packageSupplier = lines.filter(line -> line.startsWith("PackageSupplier: ")).findFirst().orElse("");
            assertThat(packageSupplier).isEqualTo("PackageSupplier: NOASSERTION");
        }

        @Test
        void verifyPackageOrder() {
            ArrayList<String> packages = new ArrayList<>(List.of("PackageName: Namespace/Project", "PackageName: Namespace/Package"));
            ArrayList<String> packageNames = lines.filter(line -> line.startsWith("PackageName: ")).collect(Collectors.toCollection(ArrayList::new));
            assertThat(packages).isEqualTo(packageNames);
        }

        @Test
        void verifySBOMCreatedTime() {
            String isoDate = isoFormat.format(localDateTime);
            String created = lines.filter(line -> line.startsWith("Created: ")).collect(Collectors.joining(""));
            assertThat(created).isEqualTo("Created: " + isoDate);
        }
    }
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
