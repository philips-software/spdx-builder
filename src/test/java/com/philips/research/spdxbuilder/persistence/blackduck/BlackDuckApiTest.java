/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.philips.research.spdxbuilder.core.domain.License;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.ComponentVersionJson;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckApi.LicenseJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackDuckApiTest {
    private static final String NAME = "Name";

    @Nested
    class ComponentLicense {
        private static final String LICENSE = "Apache-2.0";
        private static final String LICENSE2 = "MIT";

        private final ComponentVersionJson component = new ComponentVersionJson();

        @BeforeEach
        void beforeEach() {
            final var licenseJson = new LicenseJson();
            licenseJson.spdxId = LICENSE;
            licenseJson.licenseDisplay = NAME;

            component.licenses.add(licenseJson);
        }

        @Test
        void prefersSpdxLicense() {
            assertThat(component.getLicense()).contains(License.of(LICENSE));
        }

        @Test
        void fallsBackToDisplayedLicense() {
            component.licenses.get(0).spdxId = null;

            assertThat(component.getLicense().toString()).contains("LicenseRef");
        }

        @Test
        void ignoresUnknownLicense() {
            component.licenses.get(0).spdxId = null;
            component.licenses.get(0).licenseDisplay = "Unknown License";

            assertThat(component.getLicense()).isEmpty();
        }

        @Test
        void combinesConjunctiveByDefault() {
            final var json1 = new LicenseJson();
            json1.spdxId = LICENSE;
            final var json2 = new LicenseJson();
            json2.spdxId = LICENSE2;
            component.licenses.get(0).licenses = List.of(json1, json2);

            final var license = component.getLicense();

            assertThat(license).contains(License.of(LICENSE).and(License.of(LICENSE2)));
        }

        @Test
        void combinesDisjunctive() {
            final var json1 = new LicenseJson();
            json1.spdxId = LICENSE;
            final var json2 = new LicenseJson();
            json2.spdxId = LICENSE2;
            component.licenses.get(0).licenseType = "DISJUNCTIVE";
            component.licenses.get(0).licenses = List.of(json1, json2);

            final var license = component.getLicense();

            assertThat(license).contains(License.of(LICENSE).or(License.of(LICENSE2)));
        }

        @Test
        void ignoresUnknownElementsInCombinedLicense() {
            final var json1 = new LicenseJson();
            json1.spdxId = LICENSE;
            final var json2 = new LicenseJson();
            json2.licenseDisplay = "Unknown License";
            component.licenses.get(0).licenseType = "DISJUNCTIVE";
            component.licenses.get(0).licenses = List.of(json1, json2);

            final var license = component.getLicense();

            assertThat(license).contains(License.of(LICENSE));
        }

        @Test
        void ignoresEffectivelyEmptyCombinedLicense() {
            final var json = new LicenseJson();
            json.licenseDisplay = "Unknown License";
            component.licenses.get(0).licenseType = "DISJUNCTIVE";
            component.licenses.get(0).licenses = List.of(json);

            final var license = component.getLicense();

            assertThat(license).isEmpty();
        }
    }

    @Nested
    class Origin {
        @Test
        void defaultsToDecodingExternalId() {
            final var json = new BlackDuckApi.OriginJson();
            json.externalNamespace = "maven";
            json.externalId = "group:name:version";

            assertThat(json.getPurl()).isNotEmpty();
        }

        @Test
        void noPackageUrl_unknownExternalId() {
            final var json = new BlackDuckApi.OriginJson();
            json.externalNamespace = "unknown";

            assertThat(json.getPurl()).isEmpty();
        }
    }
}
