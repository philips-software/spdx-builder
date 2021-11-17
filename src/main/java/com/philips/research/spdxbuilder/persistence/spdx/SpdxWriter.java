/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.BomProcessor;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.LicenseDictionary;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts a bill-of-materials to an SPDX file.
 */
public class SpdxWriter implements BomProcessor, AutoCloseable {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));
    private static final List<String> SUPPORTED_HASH_KEYS =
            List.of("SHA1", "SHA224", "SHA256", "SHA384", "SHA512", "MD2", "MD4", "MD5", "MD6");

    private final OutputStream stream;
    private final Map<Package, SpdxRef> identifiers = new LinkedHashMap<>();

    private int nextId = 1;

    public SpdxWriter(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void process(BillOfMaterials bom) {
        try (final var doc = new TagValueDocument(this.stream)) {
            writeDocumentInformation(doc, bom);
            generatePackageIdentifiers(bom);
            writePackages(doc, bom);
            writeCustomLicenses(doc);
            System.out.println("Total: " + bom.getPackages().size() + " packages and " + bom.getRelations().size() + " relations");
        } catch (IOException | DateTimeException e) {
            throw new SpdxException("Could not write SPDX file: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    private void generatePackageIdentifiers(BillOfMaterials bom) {
        bom.getPackages().forEach(this::identifierFor);
    }

    private void writeDocumentInformation(TagValueDocument doc, BillOfMaterials bom) throws IOException, DateTimeException {
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
        doc.addValue("Creator", SpdxParty.from(bom.getOrganization()));
        final var application = this.getClass().getPackage().getImplementationTitle();
        final var version = this.getClass().getPackage().getImplementationVersion();
        doc.addValue("Creator", SpdxParty.tool(application, version));
        doc.addValue("Created", DATE_TIME_FORMAT.format(bom.getCreatedAt().isPresent() ? bom.getCreatedAt().get() : Instant.now()));
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
        doc.addComment("Start of package " + pkg);
        doc.addValue("PackageName", pkg.getFullName());
        doc.addValue("SPDXID", identifierFor(pkg));
        doc.addValue("PackageVersion", pkg.getVersion());
        doc.optionallyAddValue("PackageFileName", pkg.getFilename());
        doc.optionallyAddValue("PackageSummary", pkg.getSummary());
        doc.optionallyAddValue("PackageDescription", pkg.getDescription());
        doc.addValue("PackageHomePage", pkg.getHomePage());
        doc.optionallyAddValue("packageAttributionText", pkg.getAttribution());
        if (pkg.isInternal()) {
            doc.optionallyAddValue("PackageSupplier", bom.getOrganization().map(SpdxParty::from));
        } else {
            doc.optionallyAddValue("ExternalRef", pkg.getPurl().map(ExternalReference::new));
            Optional<SpdxParty> supplier = pkg.getSupplier().map(SpdxParty::from);
            doc.addValue("PackageSupplier", supplier.isPresent() ? supplier : null);
        }
        doc.optionallyAddValue("PackageOriginator", pkg.getOriginator().map(SpdxParty::from));
        doc.addValue("PackageDownloadLocation", pkg.getDownloadLocation());
        for (Map.Entry<String, String> entry : pkg.getHashes().entrySet()) {
            final var key = entry.getKey().replaceAll("-", "").toUpperCase();
            if (SUPPORTED_HASH_KEYS.contains(key)) {
                final var hex = entry.getValue().toLowerCase();
                doc.addValue("PackageChecksum", key + ": " + hex);
            }
        }
        doc.addValue("PackageLicenseConcluded", pkg.getConcludedLicense());
        doc.addValue("PackageLicenseDeclared", pkg.getDeclaredLicense());
        if (pkg.getDeclaredLicense().isEmpty() && pkg.getConcludedLicense().isEmpty()) {
            System.err.println("WARNING: No license declared for package " + pkg);
        }
        for (var license : pkg.getDetectedLicenses()) {
            doc.addValue("PackageLicenseInfoFromFiles", license);
        }
        doc.addValue("FilesAnalyzed", !pkg.getDetectedLicenses().isEmpty());
        doc.addValue("PackageCopyrightText", pkg.getCopyright());
        addPackageRelationships(doc, pkg, bom);
        doc.addEmptyLine();
    }

    private void addPackageRelationships(TagValueDocument doc, Package pkg, BillOfMaterials bom) throws IOException {
        for (Relation rel : bom.getRelations()) {
            if (rel.getFrom() == pkg) {
                String value = String.format(relationFormat(rel.getType()),
                        identifierFor(rel.getFrom()),
                        identifierFor(rel.getTo()));
                doc.addValue("Relationship", value);
            }
        }
    }

    private String relationFormat(Relation.Type type) {
        switch (type) {
            case DESCENDANT_OF:
                return "%s DESCENDANT_OF %s";
            case DYNAMICALLY_LINKS:
                return "%s DYNAMIC_LINK %s";
            case STATICALLY_LINKS:
                return "%s STATIC_LINK %s";
            case CONTAINS:
                return "%s CONTAINS %s";
            case DEPENDS_ON:
                return "%s DEPENDS_ON %s";
            case DEVELOPED_USING:
                return "%2$s DEV_DEPENDENCY_OF %1$s";
            default:
                System.out.println("WARNING: Unmapped relationship type: " + type);
                return "%s DEPENDS_ON %s";
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

