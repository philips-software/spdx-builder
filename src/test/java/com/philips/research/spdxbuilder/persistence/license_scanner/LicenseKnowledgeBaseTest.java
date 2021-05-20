/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.license_scanner;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.License;
import com.philips.research.spdxbuilder.core.domain.Package;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LicenseKnowledgeBaseTest {
    private static final URI LOCATION = URI.create("https://example.com");
    private static final License LICENSE = License.of("MIT");
    private static final PackageURL PURL = purlFrom("pkg:maven/namespace/name@version");

    private final Package pkg = new Package("Namespace", "Name", "Version").setPurl(PURL);
    private final BillOfMaterials bom = new BillOfMaterials().addPackage(pkg);
    private final LicenseScannerClient client = mock(LicenseScannerClient.class);
    private final LicenseKnowledgeBase knowledgeBase = new LicenseKnowledgeBase(client);

    static PackageURL purlFrom(String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    void checksLicenses() {
        pkg.setSourceLocation(LOCATION);
        final var info = new LicenseScannerClient.LicenseInfo(LICENSE.toString(), false);
        when(client.scanLicense(PURL, LOCATION)).thenReturn(Optional.of(info));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getDetectedLicenses()).contains(LICENSE);
        verify(client, never()).contest(any(), any());
    }

    @Test
    void contestsUnconfirmedLicense() {
        pkg.setDeclaredLicense(LICENSE);
        final var info = new LicenseScannerClient.LicenseInfo("Other", false);
        when(client.scanLicense(eq(PURL), any())).thenReturn(Optional.of(info));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getConcludedLicense()).isEmpty();
        verify(client).contest(PURL, LICENSE.toString());
    }

    @Test
    void acceptsConfirmedLicense() {
        pkg.setDeclaredLicense(License.of("Other"));
        final var info = new LicenseScannerClient.LicenseInfo(LICENSE.toString(), true);
        when(client.scanLicense(eq(PURL), any())).thenReturn(Optional.of(info));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getConcludedLicense()).contains(LICENSE);
        verify(client, never()).contest(any(), any());
    }

    @Test
    void ignoresCommunicationExceptions() {
        pkg.setDeclaredLicense(License.of("Other"));
        when(client.scanLicense(any(), any())).thenThrow(new LicenseScannerException("Test"));

        knowledgeBase.enhance(bom);
    }
}
