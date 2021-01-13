/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence;

import com.philips.research.spdxbuilder.core.ConversionStore;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.persistence.license.LicenseScannerClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversionPersistenceTest {
    private static final String LICENSE = "License";

    final Package pkg = new Package("Type", "Namespace", "Name", "Version");
    final LicenseScannerClient client = mock(LicenseScannerClient.class);
    final ConversionStore store = new ConversionPersistence(client);

    @Test
    void ignoresLicenseDetection_noServer() {
        final var store = new ConversionPersistence((URI) null);

        assertThat(store.detectLicense(pkg)).isEmpty();
    }

    @Test
    void detectsLicenses() {
        final var info = new ConversionStore.LicenseInfo(LICENSE, false);
        when(client.scanLicense(pkg)).thenReturn(Optional.of(info));

        final var result = store.detectLicense(pkg);

        assertThat(result).contains(info);
    }
}
