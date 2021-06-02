/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

class OrtConfiguration {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE);

    Document document = new Document();
    List<Project> projects = new ArrayList<>();
    List<Curation> curations = new ArrayList<>();

    static OrtConfiguration parse(InputStream stream) {
        try (final var reader = new InputStreamReader(stream)) {
            final OrtConfiguration ortConfiguration = MAPPER.readValue(reader, OrtConfiguration.class);
            validate(ortConfiguration);
            return ortConfiguration;
        } catch (MismatchedInputException e) {
            final var location = e.getLocation();
            throw new IllegalArgumentException("Configuration format error at line " + location.getLineNr()
                    + ", column " + location.getColumnNr());
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed configuration file: ", e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void validate(OrtConfiguration ortConfiguration) {
        if (ortConfiguration == null) {
            throw new IllegalArgumentException("Configuration is empty");
        }
        if (ortConfiguration.document == null) {
            throw new IllegalArgumentException("Configuration contains empty 'document' section");
        }
        if (ortConfiguration.projects == null) {
            throw new IllegalArgumentException("Configuration contains empty 'projects' section");
        }
        if (ortConfiguration.curations == null) {
            throw new IllegalArgumentException("Configuration contains empty 'curations' section");
        }
    }

    static String example() {
        final var config = new OrtConfiguration();
        config.document.title = "<(Optional) Document title>";
        config.document.comment = "<(Optional) Document comment>";
        config.document.namespace = URI.create("http://optional/document/namespace/uri");
        config.document.organization = "<(Optional) Organization name>";
        config.document.key = "<(Optional) Document key>";

        final var project = new Project();
        project.id = "<Input project identifier>";
        project.purl = URI.create("pkg:type/namespace/name@version");
        project.excluded = List.of("scope", "test*");
        config.projects.add(project);

        final var curation = new Curation();
        curation.purl = URI.create("pkg:type/namespace/name@version");
        curation.source = URI.create("https://optional/source/location/uri");
        curation.license = "<(Optional) License>";
        config.curations.add(curation);

        try {
            return MAPPER.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate configuration example", e);
        }
    }

    static class Document {
        String title = "";
        String organization = "";
        String comment = "";
        @NullOr String key = "";
        @NullOr URI namespace;
    }

    //TODO This seems only relevant for ORT import
    static class Project {
        String id;
        @NullOr URI purl;
        @NullOr List<String> excluded;
    }

    //TODO Add way to mark internal packages using wildcard
    static class Curation {
        URI purl;
        //FIXME Is the source code location even appropriate here? (drop)
        @NullOr URI source;
        @NullOr String license;

        PackageURL getPurl() {
            try {
                return new PackageURL(purl.toASCIIString());
            } catch (MalformedPackageURLException e) {
                throw new IllegalArgumentException("Not a valid package URL: " + purl);
            }
        }
    }
}
