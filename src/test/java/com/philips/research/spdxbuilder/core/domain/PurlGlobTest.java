/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurlGlobTest {
    private static final String TYPE = "type";
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String OTHER = "other";
    private static final String BASE = String.format("%s/%s/%s", TYPE, NAMESPACE, NAME);
    private static final String FULL_PURL = String.format("pkg:%s@%s", BASE, VERSION);
    private static final PackageURL PURL = toPurl(FULL_PURL);

    static PackageURL toPurl(String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    void matchesPerPart() {
        assertThat(new PurlGlob(FULL_PURL).matches(PURL)).isTrue();
        assertThat(new PurlGlob(TYPE + '/' + NAMESPACE + '/' + NAME).matches(PURL)).isTrue();
        assertThat(new PurlGlob(TYPE + '/' + NAME).matches(PURL)).isTrue();
        assertThat(new PurlGlob(NAME).matches(PURL)).isTrue();
    }

    @Test
    void failsPerPart() {
        assertThat(new PurlGlob(OTHER + '/' + NAMESPACE + '/' + NAME).matches(PURL)).isFalse();
        assertThat(new PurlGlob(TYPE + '/' + OTHER + '/' + NAME).matches(PURL)).isFalse();
        assertThat(new PurlGlob(TYPE + '/' + NAMESPACE + '/' + OTHER).matches(PURL)).isFalse();
    }

    @Test
    void ignoresExtraParts() {
        assertThat(new PurlGlob(FULL_PURL).matches(toPurl(FULL_PURL + "#subpath"))).isTrue();
        assertThat(new PurlGlob(FULL_PURL).matches(toPurl(FULL_PURL + "?qualifiers"))).isTrue();
    }

    @Test
    void throws_missingNamePart() {
        assertThatThrownBy(() -> new PurlGlob("@version"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("glob");
    }

    @Test
    void throws_invalidPackageUrl() {
        assertThatThrownBy(() -> new PurlGlob("not/a/valid/purl"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("glob");
    }

    @Test
    void matchesWildcard() {
        final var glob = new PurlGlob("A*B");

        assertThat(glob.matches(toPurl("pkg:type/AB"))).isTrue();
        assertThat(glob.matches(toPurl("pkg:type/AsomethingB"))).isTrue();
        assertThat(glob.matches(toPurl("pkg:type/AB@version"))).isTrue();
        assertThat(glob.matches(toPurl("pkg:type/xAsomethingB"))).isFalse();
        assertThat(glob.matches(toPurl("pkg:type/AsomethingBx"))).isFalse();
    }
}
