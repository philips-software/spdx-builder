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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final ConversionStore store = mock(ConversionStore.class);
    private final BillOfMaterials bom = new BillOfMaterials();
    private final ConversionService interactor = new ConversionInteractor(store, bom);
    private final Package project = new Package(TYPE, GROUP, PROJECT, VERSION);
    private final Package pkg = new Package(TYPE, GROUP, NAME, VERSION);

    @Test
    void setsDocumentProperties() {
        interactor.setDocument(PROJECT, ORGANIZATION);
        interactor.setComment(COMMENT);

        assertThat(bom.getTitle()).isEqualTo(PROJECT);
        assertThat(bom.getOrganization()).contains(ORGANIZATION);
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
    class OrtFile {
        @BeforeEach
        void beforeEach() {
            interactor.readOrtAnalysis(ORT_FILE);
        }

        @Test
        void processesOrtAnalysisFile() {
            verify(store).read(bom, Map.of(), ConversionStore.FileType.ORT, ORT_FILE);
        }

    }

    @Nested
    class LicenseScanner {
        @Test
        void scansProjectAndPackageLicenses() {
            bom.addProject(project);
            bom.addPackage(pkg.setLocation(LOCATION));
            final var licenseInfo = new ConversionStore.LicenseInfo(LICENSE, false);
            when(store.detectLicense(project)).thenReturn(Optional.of(licenseInfo));
            when(store.detectLicense(pkg)).thenReturn(Optional.of(licenseInfo));

            interactor.scanLicenses();

            assertThat(project.getDetectedLicense()).contains(LICENSE);
            assertThat(pkg.getDetectedLicense()).contains(LICENSE);
        }

        @Test
        void overridesConcludedLicenseIfScanConfirmed() {
            pkg.setDeclaredLicense("Other");
            bom.addPackage(pkg);
            final var licenseInfo = new ConversionStore.LicenseInfo(LICENSE, true);
            when(store.detectLicense(pkg)).thenReturn(Optional.of(licenseInfo));

            interactor.scanLicenses();

            assertThat(pkg.getDetectedLicense()).contains(LICENSE);
            //noinspection OptionalGetWithoutIsPresent
            assertThat(pkg.getDeclaredLicense().get()).isNotEqualTo(LICENSE);
            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        }
    }

    @Nested
    class Curation {
        private final Package otherPkg = new Package(TYPE, GROUP, NAME, VERSION);

        @BeforeEach
        void setUp() {
            pkg.setPurl(PURL);
            bom.addPackage(pkg).addPackage(otherPkg);
        }

        @Test
        void curatesPackageLicense() {
            interactor.curatePackageLicense(PURL, LICENSE);

            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
            assertThat(otherPkg.getConcludedLicense()).isEmpty();
        }

        @Test
        void curatesPackageSource() {
            interactor.curatePackageSource(PURL, LOCATION);

            assertThat(pkg.getLocation()).contains(LOCATION);
            assertThat(otherPkg.getLocation()).isEmpty();
        }
    }
}
