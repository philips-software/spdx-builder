/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.License;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ComponentJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.LicenseJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlackDuckApiTest {
    private static final String NAME = "Name";

    @Nested
    class ComponentLicense {
        private static final String LICENSE = "Apache-2.0";
        private static final String LICENSE2 = "MIT";

        private final ComponentJson component = new ComponentJson();

        @BeforeEach
        void beforeEach() {
            final var licenseJson = new LicenseJson();
            licenseJson.spdxId = LICENSE;
            licenseJson.licenseDisplay = NAME;

            component.licenses.add(licenseJson);
        }

        @Test
        void prefersSpdxLicense() {
            assertThat(component.getLicense()).isEqualTo(License.of(LICENSE));
        }

        @Test
        void fallsBackToDisplayedLicense() {
            component.licenses.get(0).spdxId = null;

            assertThat(component.getLicense().toString()).contains("LicenseRef");
        }

        @Test
        void combinesConjunctiveByDefault() {
            final var json2 = new LicenseJson();
            json2.spdxId = LICENSE2;
            component.licenses.add(json2);

            final var license = component.getLicense();

            assertThat(license).isEqualTo(License.of(LICENSE).and(License.of(LICENSE2)));
        }

        @Test
        void combinesDisjunctive() {
            final var json2 = new LicenseJson();
            json2.spdxId = LICENSE2;
            component.licenseType = "DISJUNCTIVE";
            component.licenses.add(json2);

            final var license = component.getLicense();

            assertThat(license).isEqualTo(License.of(LICENSE).or(License.of(LICENSE2)));
        }
    }

    @Nested
    class PackageUrlConversion {
        @Test
        void convertsOriginToPackageURL() throws Exception {
            assertPurl("maven", "Group:Name:Version", new PackageURL("pkg:maven/Group/Name@Version"));
            assertPurl("maven", "::Name:Version", new PackageURL("pkg:maven/Name@Version"));
            assertPurl("maven", "Name:Version", new PackageURL("pkg:maven/Name@Version"));
            assertPurl("UNKNOWN", "Name/Version", new PackageURL("pkg:generic/Name@Version"));
            assertPurl("npmjs", "Name/Version", new PackageURL("pkg:npm/Name@Version"));
        }

        private void assertPurl(String externalNamespace, String externalId, PackageURL purl) {
            final var origin = new BlackDuckApi.OriginJson();
            origin.externalNamespace = externalNamespace;
            origin.externalId = externalId;
            assertThat(origin.getPurl()).isEqualTo(purl);
        }
    }
}
