/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.core.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LicenseDictionary {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE);
    private static final URL LICENSES = LicenseDictionary.class.getResource("/licenses.json");
    private static final URL EXCEPTIONS = LicenseDictionary.class.getResource("/exceptions.json");
    private static final String PREFIX = "LicenseRef-";
    private static final Map<String, Function<LicenseDictionary, License>> UPGRADE_MAP = new HashMap<>();

    private static @NullOr LicenseDictionary instance;

    static {
        UPGRADE_MAP.put("apl-1.0", dict -> dict.licenseFor("AGPL-1.0-only"));
        UPGRADE_MAP.put("agpl-1.0", dict -> dict.licenseFor("AGPL-1.0-or-later"));
        UPGRADE_MAP.put("apl-3.0", dict -> dict.licenseFor("AGPL-3.0-only"));
        UPGRADE_MAP.put("agpl-3.0", dict -> dict.licenseFor("AGPL-3.0-or-later"));
        UPGRADE_MAP.put("bsd-2-clause-FreeBSD", dict -> dict.licenseFor("BSD-2-Clause-Views"));
        UPGRADE_MAP.put("bsd-2-clause-NetBSD", dict -> dict.licenseFor("BSD-2-Clause"));
        UPGRADE_MAP.put("ecos-2.0", dict -> dict.licenseFor("eCos license version 2.0"));
        UPGRADE_MAP.put("gfdl-1.1", dict -> dict.licenseFor("GNU Free Documentation License v1.1"));
        UPGRADE_MAP.put("gfdl-1.2", dict -> dict.licenseFor("GNU Free Documentation License v1.2"));
        UPGRADE_MAP.put("gfdl-1.3", dict -> dict.licenseFor("GNU Free Documentation License v1.3"));
        UPGRADE_MAP.put("gpl-1.0", dict -> dict.licenseFor("GPL-1.0-only"));
        UPGRADE_MAP.put("gpl-1.0+", dict -> dict.licenseFor("GPL-1.0-or-later"));
        UPGRADE_MAP.put("gpl-2.0", dict -> dict.licenseFor("GPL-2.0-only"));
        UPGRADE_MAP.put("gpl-2.0+", dict -> dict.licenseFor("GPL-2.0-or-later"));
        UPGRADE_MAP.put("gpl-2.0-with-autoconf-exception", dict -> dict.licenseFor("GPL-2.0").with("Autoconf-exception-2.0"));
        UPGRADE_MAP.put("gpl-2.0-with-bison-exception", dict -> dict.licenseFor("GPL-2.0").with("Bison-exception-2.2"));
        UPGRADE_MAP.put("gpl-2.0-with-classpath-exception", dict -> dict.licenseFor("GPL-2.0").with("Classpath-exception-2.0"));
        UPGRADE_MAP.put("gpl-2.0-with-font-exception", dict -> dict.licenseFor("GPL-2.0").with("Font-exception-2.0"));
        UPGRADE_MAP.put("gpl-2.0-with-GCC-exception", dict -> dict.licenseFor("GPL-2.0").with("GCC-exception-2.0"));
        UPGRADE_MAP.put("gpl-3.0", dict -> dict.licenseFor("GPL-3.0-only"));
        UPGRADE_MAP.put("gpl-3.0+", dict -> dict.licenseFor("GPL-3.0-or-later"));
        UPGRADE_MAP.put("gpl-3.0-with-autoconf-exception", dict -> dict.licenseFor("GPL-3.0").with("Autoconf-exception-3.0"));
        UPGRADE_MAP.put("gpl-3.0-with-GCC-exception", dict -> dict.licenseFor("GPL-3.0").with("GCC-exception-3.0"));
        UPGRADE_MAP.put("lgpl-2.0", dict -> dict.licenseFor("LGPL-2.0-only"));
        UPGRADE_MAP.put("lgpl-2.0+", dict -> dict.licenseFor("LGPL-2.0-or-later"));
        UPGRADE_MAP.put("lgpl-2.1", dict -> dict.licenseFor("LGPL-2.1-only"));
        UPGRADE_MAP.put("lgpl-2.1+", dict -> dict.licenseFor("LGPL-2.1-or-later"));
        UPGRADE_MAP.put("lgpl-3.0", dict -> dict.licenseFor("LGPL-3.0-only"));
        UPGRADE_MAP.put("lgpl-3.0+", dict -> dict.licenseFor("LGPL-3.0-or-later"));
        UPGRADE_MAP.put("nunit", dict -> dict.licenseFor("MIT-advertising"));
        UPGRADE_MAP.put("standardml-nj", dict -> dict.licenseFor("Standard ML of New Jersey License"));
        UPGRADE_MAP.put("wxwindows", dict -> dict.licenseFor("wxWindows Library License"));
    }

    private final String version;
    private final Map<String, String> spdxIdentifiers = new HashMap<>();
    private final Map<String, Integer> customIdentifiers = new HashMap<>();
    private final Map<Integer, String> customLicenses = new HashMap<>();
    private final Map<String, String> spdxExceptions = new HashMap<>();
    private int nextCustomId = 1;

    LicenseDictionary() {
        version = loadLicenses();
        loadExceptions();
    }

    public static LicenseDictionary getInstance() {
        if (instance == null) {
            instance = new LicenseDictionary();
        }

        return instance;
    }

    private String loadLicenses() {
        try {
            final var json = MAPPER.readValue(LICENSES, LicensesJson.class);
            json.licenses.stream()
                    .filter(lic -> !lic.isDeprecatedLicenseId)
                    .forEach(lic -> spdxIdentifiers.put(lic.licenseId.toLowerCase(), lic.licenseId));
            return json.licenseListVersion;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read SPDX licenses from " + LICENSES, e);
        }
    }

    private void loadExceptions() {
        try {
            final var json = MAPPER.readValue(EXCEPTIONS, LicensesJson.class);
            json.exceptions.stream()
                    .filter(lic -> !lic.isDeprecatedLicenseId)
                    .forEach(lic -> spdxExceptions.put(lic.licenseExceptionId.toLowerCase(), lic.licenseExceptionId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read SPDX exceptions from " + EXCEPTIONS, e);
        }
    }

    void clear() {
        customLicenses.clear();
        customIdentifiers.clear();
        nextCustomId = 1;
    }

    public String getVersion() {
        return version;
    }

    public License licenseFor(String identifier) {
        if (identifier.isBlank()) {
            return License.NONE;
        }
        return getSpdxLicense(identifier)
                .orElseGet(() -> upgrade(identifier)
                        .orElseGet(() -> getOrCreateCustomLicense(identifier)));
    }

    private Optional<License> upgrade(String identifier) {
        final var fn = UPGRADE_MAP.get(identifier.trim().toLowerCase());
        return (fn != null)
                ? Optional.of(fn.apply(this))
                : Optional.empty();
    }

    public License withException(License license, String exception) {
        if (exception.isBlank()) {
            return license;
        }
        final var ex = spdxExceptions.get(exception.trim().toLowerCase());
        return (ex != null)
                ? license.with(ex)
                : licenseFor(license + " WITH " + exception.trim());
    }

    private Optional<License> getSpdxLicense(String identifier) {
        final var id = spdxIdentifiers.get(identifier.trim().toLowerCase());
        return (id != null) ? Optional.of(License.of(id)) : Optional.empty();
    }

    private License getOrCreateCustomLicense(String identifier) {
        final var customId = PREFIX + customIdentifiers.computeIfAbsent(identifier.trim().toLowerCase(), x -> {
            customLicenses.put(nextCustomId, identifier.trim());
            return nextCustomId++;
        });
        return License.of(customId);
    }

    public Map<String, String> getCustomLicenses() {
        return customLicenses.entrySet().stream()
                .collect(Collectors.toMap(e -> PREFIX + e.getKey(), Map.Entry::getValue));
    }

    @SuppressWarnings({"NotNullFieldNotInitialized", "MismatchedQueryAndUpdateOfCollection"})
    private static class LicensesJson {
        String licenseListVersion;
        List<LicenseJson> licenses;
        List<LicenseJson> exceptions;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private static class LicenseJson {
        boolean isDeprecatedLicenseId;
        String licenseId;
        String licenseExceptionId;
    }
}
