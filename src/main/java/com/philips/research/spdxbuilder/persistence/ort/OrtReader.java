/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.ort;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;
import com.philips.research.spdxbuilder.core.bom.Party;
import com.philips.research.spdxbuilder.persistence.BillOfMaterialsStore;

import java.io.File;
import java.io.IOException;

public class OrtReader implements BillOfMaterialsStore {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    public BillOfMaterials read(File file) {
        try {
            final var yaml = MAPPER.readValue(file, OrtJson.class);
            final var result = yaml.analyzer.result;

            final var bom = new BillOfMaterials();
            for (PackageBaseJson project : result.projects) {
                bom.addProject(readPackageJson(project));
            }
            for (PackageBaseJson pkg : result.getPackages()) {
                bom.addDependency(readPackageJson(pkg));
            }
            return bom;
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }

    private Package readPackageJson(PackageBaseJson pkg) {
        final var result = new Package(pkg.getNamespace(), pkg.getName(), pkg.getVersion());
        result.setSupplier(new Party(Party.Type.ORGANIZATION, pkg.getNamespace()));
        result.setDeclaredLicense(pkg.getSpdxLicense());
        result.setDescription(pkg.description);
        result.setHomePage(pkg.homepage);
        if (pkg.getSourceArtifact() != null && !pkg.getSourceArtifact().url.getPath().isEmpty()) {
            result.setLocation(pkg.getSourceArtifact().url);
            if (!pkg.getSourceArtifact().getHash().algorithm.isEmpty()) {
                result.addHash(pkg.getSourceArtifact().getHash().algorithm, pkg.getSourceArtifact().getHash().value);
            }
        }
        return result;
    }

    public void write(File file, BillOfMaterials bom) {
        // Not implemented
    }
}
