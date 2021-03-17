/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Recursive parser configurer using parameters stored in an external file.
 */
class TreeFormats {
    public static final String FORMATS_FILE = "/treeformats.yml";
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private final List<FormatDefinition> formats;

    public TreeFormats() {
        formats = readFormats();
    }

    private List<FormatDefinition> readFormats() {
        try (final InputStream stream = TreeFormats.class.getResourceAsStream(FORMATS_FILE)) {
            return MAPPER.readValue(stream, Formats.class).formats;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load tree formats", e);
        }
    }

    void configure(TreeParser parser, String format) {
        final var definition = formats.stream()
                .filter(fmt -> format.equals(fmt.format))
                .findFirst()
                .orElseThrow(() -> new TreeException("Undefined tree format: " + format));
        if (definition.parent != null) {
            configure(parser, definition.parent);
        }
        definition.format(parser);
    }

    void printFormats() {
        formats.forEach(fmt -> {
            System.out.println("Format: '" + fmt.format + "'");
            if (!fmt.tool.isBlank()) {
                System.out.println("'\nTool example: " + fmt.tool);
            }
            System.out.println(fmt.description);
        });
    }

    static class Formats {
        List<FormatDefinition> formats = new ArrayList<>();
    }

    static class FormatDefinition {
        String format = "?";
        String description = "";
        String tool = "";
        @NullOr String parent;
        @NullOr MatchMask type;
        @NullOr Map<String, String> types;
        @NullOr String cleanup;
        @NullOr String skip;
        @NullOr String identifier;
        @NullOr MatchMask namespace;
        @NullOr MatchMask name;
        @NullOr MatchMask version;
        @NullOr String start;
        @NullOr String end;
        @NullOr MatchMask relationship;
        @NullOr Map<String, String> relationships;

        void format(TreeParser parser) {
            applyMask(type, parser::withType);
            applyMapping(types, parser::withTypes);

            applyRegex(cleanup, parser::withCleanup);
            applyRegex(skip, parser::withSkip);
            applyRegex(identifier, parser::withIdentifier);
            applyMask(namespace, parser::withNamespace);
            applyMask(name, parser::withName);
            applyMask(version, parser::withVersion);

            applyRegex(start, parser::withStartSection);
            applyRegex(end, parser::withEndSection);

            applyMask(relationship, parser::withRelationship);
            applyMapping(relationships, parser::withRelationships);
        }

        private void applyRegex(@NullOr String regex, Consumer<String> property) {
            if (regex != null) {
                property.accept(regex);
            }
        }

        private void applyMask(@NullOr MatchMask mask, BiConsumer<String, Integer> property) {
            if (mask != null && mask.regex != null) {
                property.accept(mask.regex, mask.group);
            }
        }

        private void applyMapping(@NullOr Map<String, String> mapping, Consumer<Map<String, String>> property) {
            if (mapping != null) {
                property.accept(mapping);
            }
        }
    }

    static class MatchMask {
        @NullOr String regex;
        int group = 1;
    }
}
