/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.LicenseDictionary;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts a bill-of-materials to an SPDX file.
 */
public class SpdxWriter implements BomWriter {
    private static final List<String> SUPPORTED_HASH_KEYS = List.of("SHA1", "SHA224", "SHA256", "SHA384", "SHA512", "MD2", "MD4", "MD5", "MD6");

    private final File file;
    private final Map<Package, SpdxRef> identifiers = new HashMap<>();

    private int nextId = 1;

    public SpdxWriter(File file) {
        this.file = file;
    }

    @Override
    public void write(BillOfMaterials bom) {
        System.out.println("Writing SBOM to '" + file + "'");
        try (final var doc = new TagValueDocument(new FileOutputStream(file))) {
            writeDocumentInformation(doc, bom);
            generatePackageIdentifiers(bom);
            writePackages(doc, bom);
            writeCustomLicenses(doc);
            System.out.println("Total: " + bom.getPackages().size() + " packages and " + bom.getRelations().size() + " relations");
        } catch (IOException e) {
            throw new SpdxException("Could not write SPDX file: " + e.getMessage());
        }
    }

    private void generatePackageIdentifiers(BillOfMaterials bom) {
        bom.getPackages().forEach(this::identifierFor);
    }

    private void writeDocumentInformation(TagValueDocument doc, BillOfMaterials bom) throws IOException {
        doc.addValue("SPDXVersion", "SPDX-2.2");
        doc.addValue("DataLicense", SpdxLicense.of("CC0-1.0"));
        doc.addValue("SPDXID", new SpdxRef(bom.getIdentifier().orElse("DOCUMENT")));
        doc.addValue("DocumentName", bom.getTitle());
        doc.addValue("DocumentNamespace", bom.getNamespace()
                .orElseGet(() -> URI.create("https://spdx.org/spdxdocs"))
                .resolve(UUID.randomUUID().toString()));
        doc.addValue("LicenseListVersion", LicenseDictionary.getInstance().getVersion());

        doc.addEmptyLine();
        doc.addComment("Creation information");
        final var creator = bom.getOrganization().map(SpdxParty::organization);
        doc.addValue("Creator", creator);
        final var application = this.getClass().getPackage().getImplementationTitle();
        final var version = this.getClass().getPackage().getImplementationVersion();
        doc.addValue("Creator", SpdxParty.tool(application, version));
        doc.addValue("Created", Instant.now());
        doc.addValue("CreatorComment", "This SPDX file was generated by " + application + ".");
        doc.addValue("DocumentComment", bom.getComment());
        doc.addEmptyLine();
    }

    private void writePackages(TagValueDocument doc, BillOfMaterials bom) throws IOException {
        for (Package pkg : identifiers.keySet()) {
            writePackage(doc, pkg, bom);
        }
    }

    private void writePackage(TagValueDocument doc, Package pkg, BillOfMaterials bom) throws IOException {
        doc.addComment("Start of " + pkg.getType() + " package '" + pkg.getName() + "' version " + pkg.getVersion());
        doc.addValue("PackageName", pkg.getFullName());
        doc.addValue("SPDXID", identifierFor(pkg));
        doc.addValue("PackageVersion", pkg.getVersion());
        doc.optionallyAddValue("PackageFileName", pkg.getFilename());
        doc.addValue("ExternalRef", ExternalReference.purl(pkg));
        doc.optionallyAddValue("PackageSupplier", pkg.getSupplier().map(SpdxParty::from));
        doc.optionallyAddValue("PackageOriginator", pkg.getOriginator().map(SpdxParty::from));
        doc.addValue("PackageDownloadLocation", pkg.getSourceLocation());
        doc.addValue("FilesAnalyzed", pkg.getDetectedLicense().isPresent());
        for (Map.Entry<String, String> entry : pkg.getHashes().entrySet()) {
            final var key = entry.getKey().replaceAll("-", "").toUpperCase();
            if (SUPPORTED_HASH_KEYS.contains(key)) {
                final var hex = entry.getValue().toLowerCase();
                doc.addValue("PackageChecksum", key + ": " + hex);
            }
        }
        doc.addValue("PackageHomePage", pkg.getHomePage());
        doc.addValue("PackageLicenseConcluded", pkg.getConcludedLicense());
        if (pkg.getConcludedLicense().isEmpty()) {
            System.err.println("WARNING: No concluded license for package " + pkg);
        }
        doc.addValue("PackageLicenseDeclared", pkg.getDeclaredLicense());
        doc.addValue("PackageLicenseInfoFromFiles", pkg.getDetectedLicense());
        doc.addValue("PackageCopyrightText", pkg.getCopyright());
        doc.optionallyAddValue("PackageSummary", pkg.getSummary());
        doc.optionallyAddValue("PackageDescription", pkg.getDescription());
        doc.optionallyAddValue("packageAttributionText", pkg.getAttribution());
        addPackageRelationships(doc, pkg, bom);
        doc.addEmptyLine();
    }

    private void addPackageRelationships(TagValueDocument doc, Package pkg, BillOfMaterials bom) throws IOException {
        for (Relation rel : bom.getRelations()) {
            if (rel.getFrom() == pkg) {
                String value = String.format("%s %s %s", identifierFor(rel.getFrom()),
                        mappedRelationType(rel.getType()),
                        identifierFor(rel.getTo()));
                doc.addValue("Relationship", value);
            }
        }
    }

    private String mappedRelationType(Relation.Type type) {
        switch (type) {
            case DESCENDANT_OF:
                return "DESCENDANT_OF";
            case DYNAMIC_LINK:
                return "DYNAMIC_LINK";
            case STATIC_LINK:
                return "STATIC_LINK";
            case DEPENDS_ON:
            default:
                return "DEPENDS_ON";
        }
    }

    private void writeCustomLicenses(TagValueDocument doc) throws IOException {
        final var dictionary = LicenseDictionary.getInstance().getCustomLicenses();
        final var keys = dictionary.keySet().stream().sorted().collect(Collectors.toList());
        for (var key : keys) {
            doc.addValue("LicenseID", key);
            doc.addValue("LicenseName", dictionary.get(key));
        }
    }

    private SpdxRef identifierFor(Package pkg) {
        return identifiers.computeIfAbsent(pkg, (o) -> new SpdxRef(Integer.toString(nextId++)));
    }
}

