/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.philips.research.spdxbuilder.core.ConversionService;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TreeConfiguration {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    private final Configuration config;

    private TreeConfiguration(Configuration config) {
        this.config = config;
    }

    /**
     * Parses a configuration from the provided YAML input stream.
     *
     * @param stream YAML text stream
     * @return configuration
     */
    static TreeConfiguration parse(InputStream stream) {
        try (final var reader = new InputStreamReader(stream)) {
            final var config = MAPPER.readValue(reader, Configuration.class);
            return new TreeConfiguration(config);
        } catch (MismatchedInputException e) {
            throw new IllegalArgumentException("Configuration format error", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed configuration file: ", e);
        }
    }

    /**
     * Applies the generic document configuration.
     *
     * @param service target
     */
    void apply(ConversionService service) {
        final var document = config.getDocument();
        service.setDocument(document.title, document.organization);
        if (document.comment != null) {
            service.setComment(document.comment);
        }
        if (document.key != null) {
            service.setDocReference(document.key);
        }
        if (document.namespace != null) {
            service.setDocNamespace(document.namespace);
        }
    }

    List<String> getInternalGlobs() {
        return config.internal;
    }

    private static class Configuration {
        @NullOr Document document;
        List<String> internal = new ArrayList<>();

        Document getDocument() {
            return (document != null) ? document : new Document();
        }
    }

    private static class Document {
        String title = "";
        String organization = "";
        @NullOr String comment;
        @NullOr String key;
        @NullOr URI namespace;
    }
}
