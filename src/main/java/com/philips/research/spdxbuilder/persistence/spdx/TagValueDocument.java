/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import pl.tlinkowski.annotation.basic.NullOr;

import java.io.*;
import java.util.Optional;

/**
 * SPDX Tag-value format implementation.
 */
public class TagValueDocument implements Closeable {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String NO_ASSERTION = "NOASSERTION";
    private static final String NONE = "NONE";

    private final Writer writer;

    /**
     * Starts a new tag-value document.
     */
    public TagValueDocument(OutputStream stream) {
        writer = new OutputStreamWriter(stream);
    }

    /**
     * Writes a tag only if a value is present.
     *
     * @param tag   type of the value
     * @param value optional value
     */
    public void optionallyAddValue(String tag, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<?> value) throws IOException {
        if (value.isPresent()) {
            addValue(tag, value);
        }
    }

    /**
     * Writes a tag with a value, converting a <code>null</code> or empty <code>Optional</code> to "NOASSERTION"
     * and an empty string to "NONE".
     *
     * @param tag   type of the value
     * @param value <code>Optional</code> or <code>Object</code> value
     */
    public void addValue(String tag, @NullOr Object value) throws IOException {
        if (value instanceof Optional) {
            value = ((Optional<?>) value).orElse(null);
        }

        if (value == null) {
            value = NO_ASSERTION;
        }

        final var string = value.toString();
        if (string.isBlank()) {
            value = NONE;
        }
        writeLine(tag + ": " + (string.contains("\n") ? "<text>" + value + "</text>" : value));
    }

    /**
     * Writes an empty separator line.
     */
    public void addEmptyLine() throws IOException {
        writeLine("");
    }

    /**
     * Writes a comment line.
     */
    public void addComment(String comment) throws IOException {
        writeLine("## " + comment);
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.write('\n');
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

}

