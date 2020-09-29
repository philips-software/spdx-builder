/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConversionInteractorTest {
    private static final File ORT_FILE = Path.of("src", "main", "resources", "ort_sample.yml").toFile();
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String PROJECT = "Project";
    private static final String PACKAGE = "Package";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";
    private static final URI LOCATION = URI.create("www.example.com");

    private final ConversionStore store = mock(ConversionStore.class);
    private final ConversionService interactor = new ConversionInteractor(store);
    private final BillOfMaterials bom = new BillOfMaterials();
    private final Package project = new Package(TYPE, NAMESPACE, PROJECT, VERSION);
    private final Package pkg = new Package(TYPE, NAMESPACE, PACKAGE, VERSION);

    @BeforeEach
    void beforeEach() {
        when(store.read(ConversionStore.FileType.ORT, ORT_FILE)).thenReturn(bom);
        interactor.readOrtAnalysis(ORT_FILE);
    }

    @Test
    void processesOrtAnalysisFile() {
        verify(store).read(ConversionStore.FileType.ORT, ORT_FILE);
    }

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
