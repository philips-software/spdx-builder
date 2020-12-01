/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

class Configuration {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE);

    Document document = new Document();
    List<Project> projects = new ArrayList<>();
    List<Curation> curations = new ArrayList<>();

    static Configuration parse(InputStream stream) {
        try (final var reader = new InputStreamReader(stream)) {
            final Configuration configuration = MAPPER.readValue(reader, Configuration.class);
            validate(configuration);
            return configuration;
        } catch (MismatchedInputException e) {
            final var location = e.getLocation();
            throw new IllegalArgumentException("Configuration format error at line " + location.getLineNr()
                    + ", column " + location.getColumnNr());
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed configuration file: ", e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void validate(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration is empty");
        }
        if (configuration.document == null) {
            throw new IllegalArgumentException("Configuration contains empty 'document' section");
        }
        if (configuration.projects == null) {
            throw new IllegalArgumentException("Configuration contains empty 'projects' section");
        }
        if (configuration.curations == null) {
            throw new IllegalArgumentException("Configuration contains empty 'curations' section");
        }
    }

    static String example() {
        final var config = new Configuration();
        config.document.title = "<Document title>";
        config.document.comment = "<Document comment>";
        config.document.namespace = URI.create("http://document/namespace/uri");
        config.document.organization = "<Authoring organization name>";
        config.document.spdxId = "<Document SPDX identifier>";

        final var project = new Project();
        project.id = "<ORT project identifier>";
        project.purl = URI.create("pkg:type/namespace/name@version");
        config.projects.add(project);

        final var curation = new Curation();
        curation.purl = URI.create("pkg:type/namespace/name@version");
        curation.source = URI.create("https://source/location/uri");
        curation.license = "<License>";
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
        @NullOr String spdxId = "";
        @NullOr URI namespace;
    }

    static class Project {
        String id;
        @NullOr URI purl;
    }

    static class Curation {
        URI purl;
        @NullOr URI source;
        @NullOr String license;
    }
}
