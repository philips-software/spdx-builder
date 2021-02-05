/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LicenseKnowledgeBaseTest {
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "License";

    private final Package pkg = new Package("Type", "Namespace", "Name", "Version");
    private final BillOfMaterials bom = new BillOfMaterials().addPackage(pkg);
    private final LicenseScannerClient client = mock(LicenseScannerClient.class);
    private final LicenseKnowledgeBase knowledgeBase = new LicenseKnowledgeBase(client);

    @Test
    void checksLicenses() {
        pkg.setSourceLocation(LOCATION);
        final var info = new LicenseScannerClient.LicenseInfo(LICENSE, false);
        when(client.scanLicense(pkg.getPurl(), LOCATION)).thenReturn(Optional.of(info));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getDetectedLicense()).contains(LICENSE);
        verify(client, never()).contest(any(), any());
    }

    @Test
    void contestsUnconfirmedLicense() {
        pkg.setDeclaredLicense(LICENSE);
        final var info = new LicenseScannerClient.LicenseInfo("Other", false);
        when(client.scanLicense(eq(pkg.getPurl()), any())).thenReturn(Optional.of(info));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        verify(client).contest(pkg.getPurl(), LICENSE);
    }

    @Test
    void acceptsConfirmedLicense() {
        pkg.setDeclaredLicense("Other");
        final var info = new LicenseScannerClient.LicenseInfo(LICENSE, true);
        when(client.scanLicense(eq(pkg.getPurl()), any())).thenReturn(Optional.of(info));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        verify(client, never()).contest(any(), any());
    }

    @Test
    void ignoresCommunicationExceptions() {
        pkg.setDeclaredLicense("Other");
        when(client.scanLicense(any(), any())).thenThrow(new LicenseScannerException("Test"));

        knowledgeBase.enhance(bom);
    }
}
