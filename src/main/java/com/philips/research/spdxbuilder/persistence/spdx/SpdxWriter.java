/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.BillOfMaterials;
import com.philips.research.spdxbuilder.core.bom.Package;
import com.philips.research.spdxbuilder.core.bom.Relation;
import com.philips.research.spdxbuilder.persistence.BillOfMaterialsStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts a bill-of-materials to an SPDX file.
 */
public class SpdxWriter implements BillOfMaterialsStore {
    private static final List<String> SUPPORTED_HASH_KEYS = List.of("SHA1", "SHA224", "SHA256", "SHA384", "SHA512", "MD2", "MD4", "MD5", "MD6");

    private final Map<Object, SpdxRef> identifiers = new HashMap<>();

    private int nextId = 1;

    public BillOfMaterials read(File file) {
        // Not implemented
        throw new RuntimeException("Not implemented");
    }

    /**
     * Writes the bill-of-materials to an SPDX file.
     */
    public void write(File file, BillOfMaterials bom) {
        try (final var doc = new TagValueDocument(new FileOutputStream(file))) {
            //TODO where does the product name come from?
            writeDocumentInformation(doc, "product name");
            generatePackageIdentifiers(bom);
            for (Package pkg : bom.getProjects()) {
                writePackage(doc, pkg, bom);
            }
            for (Package pkg : bom.getPackages()) {
                writePackage(doc, pkg, bom);
            }
            //TODO Add non-SPDX license information
        } catch (IOException e) {
            //TODO Catch exceptions
            e.printStackTrace();
        }
    }

    private void generatePackageIdentifiers(BillOfMaterials bom) {
        bom.getProjects().forEach(this::identifierFor);
        bom.getPackages().forEach(this::identifierFor);
    }

    private void writeDocumentInformation(TagValueDocument doc, String name) throws IOException {
        String documentId = UUID.randomUUID().toString();
        doc.addValue("SPDXVersion", "SPDX-2.2");
        doc.addValue("DataLicense", SpdxLicense.of("CC0-1.0"));
        //TODO Document reference?
        doc.addValue("SPDXID", new SpdxRef("DOCUMENT"));
        //TODO Document name?
        doc.addValue("DocumentName", "Bill-of-materials for " + name);
        doc.addValue("DocumentNamespace", URI.create("https://spdx.org/spdxdocs/" + documentId));
        doc.addComment("(No external document references)");

        doc.addEmptyLine();
        doc.addComment("Creation information");
        doc.addValue("Creator", SpdxParty.organization("Koninklijke Philips N.V."));
        final var application = this.getClass().getPackage().getImplementationTitle();
        final var version = this.getClass().getPackage().getImplementationVersion();
        doc.addValue("Creator", SpdxParty.tool(application, version));
        doc.addValue("Created", Instant.now());
        doc.addText("CreatorComment", "This SPDX file was automatically generated by " + application + ".");
        doc.addText("DocumentComment", "This document was automatically generated.");
        doc.addValue("LicenseListVersion", SpdxLicense.VERSION);
        doc.addEmptyLine();
    }

    private void writePackage(TagValueDocument doc, Package pkg, BillOfMaterials bom) throws IOException {
        doc.addComment("Start of " + pkg.getType() + " package '" + pkg.getName() + "' version " + pkg.getVersion());
        doc.addValue("PackageName", pkg.getName());
        doc.addValue("SPDXID", identifierFor(pkg));
        doc.addValue("PackageVersion", pkg.getVersion());
        if (pkg.getFilename().isPresent()) {
            doc.addValue("PackageFileName", pkg.getFilename());
        }
        doc.addValue("ExternalRef", ExternalReference.purl(pkg));
        doc.addValue("PackageSupplier", SpdxParty.from(pkg.getSupplier()));
        doc.addValue("PackageOriginator", SpdxParty.from(pkg.getOriginator()));
        doc.addValue("PackageDownloadLocation", pkg.getLocation());
        doc.addValue("FilesAnalyzed", pkg.getDetectedLicense().isPresent());
        for (Map.Entry<String, String> entry : pkg.getHashes().entrySet()) {
            final var key = entry.getKey().replaceAll("-", "").toUpperCase();
            if (SUPPORTED_HASH_KEYS.contains(key)) {
                doc.addValue("PackageChecksum", key + ": " + entry.getValue());
            }
        }
        doc.addValue("PackageHomePage", pkg.getHomePage());
        doc.addValue("PackageLicenseConcluded", pkg.getConcludedLicense());
        if (pkg.getConcludedLicense().isEmpty()) {
            System.err.println("WARNING: No concluded license for package " + pkg);
        }
        doc.addValue("PackageLicenseDeclared", pkg.getDeclaredLicense());
        doc.addValue("PackageLicenseInfoFromFiles", pkg.getDetectedLicense());
        doc.addText("PackageCopyrightText", pkg.getCopyright());
        if (pkg.getSummary().isPresent()) {
            doc.addText("PackageSummary", pkg.getSummary());
        }
        if (pkg.getDescription().isPresent()) {
            doc.addText("PackageDescription", pkg.getDescription());
        }
        if (pkg.getAttribution().isPresent()) {
            doc.addText("packageAttributionText", pkg.getAttribution());
        }
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

    private void writeLicense(TagValueDocument doc) throws IOException {
//       doc.addValue("LicenseID", );
//       doc.addText("ExtractedText", );
//       doc.addValue("LicenseName", );
//       doc.addValue("LicenseCrossReference", );
    }

    private SpdxRef identifierFor(Object object) {
        return identifiers.computeIfAbsent(object, (o) -> new SpdxRef(Integer.toString(nextId++)));
    }
}

