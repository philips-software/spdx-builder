/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import com.philips.research.spdxbuilder.core.KnowledgeBase;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.LicenseParser;
import com.philips.research.spdxbuilder.core.domain.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BomBaseKnowledgeBaseTest {
    private static final String TYPE = "type";
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String HOMEPAGE = "http://example.com/home";
    private static final String ATTRIBUTION = "Attribution";
    private static final String COPYRIGHT = "Copyright";
    private static final String FILENAME = "file.name";
    private static final String SHA1 = "Sha1";
    private static final String SHA256 = "Sha256";
    private static final String ORIGINATOR = "Originator";
    private static final String SUPPLIER = "Supplier";
    private static final String SOURCE_LOCATION = "http://example.com/source";
    private static final String DECLARED_LICENSE = "Declared license";
    private static final String DETECTED_LICENSE = "Detected license";

    private final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);
    private final BillOfMaterials bom = new BillOfMaterials().addPackage(pkg);
    private final BomBaseClient client = mock(BomBaseClient.class);
    private final KnowledgeBase knowledgeBase = new BomBaseKnowledgeBase(client);
    private final PackageMetadata meta = mock(PackageMetadata.class);

    @BeforeEach
    void beforeEach() {
        when(client.readPackage(pkg.getPurl())).thenReturn(Optional.of(meta));
    }

    @Test
    void enhancesPackage() throws Exception {
        when(meta.getTitle()).thenReturn(Optional.of(TITLE));
        when(meta.getDescription()).thenReturn(Optional.of(DESCRIPTION));
        when(meta.getHomePage()).thenReturn(Optional.of(new URL(HOMEPAGE)));
        when(meta.getAttribution()).thenReturn(Optional.of(ATTRIBUTION));
//        when(meta.getCopyright()).thenReturn(Optional.of(COPYRIGHT));
//        when(meta.getFilename()).thenReturn(Optional.of(FILENAME));
        when(meta.getSha1()).thenReturn(Optional.of(SHA1));
        when(meta.getSha256()).thenReturn(Optional.of(SHA256));
//        when(meta.getSupplier()).thenReturn(Optional.of(SUPPLIER));
//        when(meta.getOriginator()).thenReturn(Optional.of(ORIGINATOR));
        when(meta.getSourceLocation()).thenReturn(Optional.of(URI.create(SOURCE_LOCATION)));
        when(meta.getDeclaredLicense()).thenReturn(Optional.of(DECLARED_LICENSE));
        when(meta.getDetectedLicense()).thenReturn(Optional.of(DETECTED_LICENSE));

        knowledgeBase.enhance(bom);

        assertThat(pkg.getSummary()).contains(TITLE);
        assertThat(pkg.getDescription()).contains(DESCRIPTION);
        assertThat(pkg.getHomePage()).contains(new URL(HOMEPAGE));
//        assertThat(pkg.getAttribution()).contains(ATTRIBUTION);
//        assertThat(pkg.getCopyright()).contains();
//        assertThat(pkg.getFilename()).isEqualTo();
        assertThat(pkg.getHashes()).isEqualTo(Map.of("SHA1", SHA1, "SHA256", SHA256));
//        assertThat(pkg.getOriginator()).contains();
//        assertThat(pkg.getSupplier()).contains();
        assertThat(pkg.getSourceLocation()).contains(URI.create(SOURCE_LOCATION));
        assertThat(pkg.getDeclaredLicense()).contains(LicenseParser.parse(DECLARED_LICENSE));
        assertThat(pkg.getDetectedLicense()).contains(LicenseParser.parse(DETECTED_LICENSE));
    }
}