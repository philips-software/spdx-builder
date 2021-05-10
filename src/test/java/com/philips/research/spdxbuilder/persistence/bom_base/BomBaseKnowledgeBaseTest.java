/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.bom_base;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
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
import static org.mockito.Mockito.*;

class BomBaseKnowledgeBaseTest {
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
    private static final PackageURL PURL = packageUrl("pkg:/maven/" + NAMESPACE + "/" + NAME + "@" + VERSION);

    private final Package pkg = new Package(NAMESPACE, NAME, VERSION).setPurl(PURL);
    private final BillOfMaterials bom = new BillOfMaterials().addPackage(pkg);
    private final BomBaseClient client = mock(BomBaseClient.class);
    private final KnowledgeBase knowledgeBase = new BomBaseKnowledgeBase(client);
    private final PackageMetadata meta = mock(PackageMetadata.class);

    static PackageURL packageUrl(String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @BeforeEach
    void beforeEach() {
        when(client.readPackage(PURL)).thenReturn(Optional.of(meta));
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

        final var success = knowledgeBase.enhance(bom);

        assertThat(success).isTrue();
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

    @Test
    void skipsEnhancementOfInternalPackages() {
        bom.addPackage(pkg.setInternal(true));

        knowledgeBase.enhance(bom);

        verify(client, never()).readPackage(any(PackageURL.class));
    }

    @Test
    void notifiesEnhancementFailure() {
        bom.addPackage(new Package(NAMESPACE, NAME, VERSION + "1").setPurl(packageUrl("pkg:maven/second@2")));
        bom.addPackage(new Package(NAMESPACE, NAME, VERSION + "2"));
        //noinspection unchecked
        when(client.readPackage(any(PackageURL.class))).thenReturn(Optional.empty(), Optional.of(meta));

        final var success = knowledgeBase.enhance(bom);

        assertThat(success).isFalse();
        verify(client, times(2)).readPackage(any(PackageURL.class));
    }
}
