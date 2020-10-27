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

import pl.tlinkowski.annotation.basic.NullOr;

import java.io.*;
import java.util.Optional;

/**
 * SPDX Tag-value format implementation.
 */
public class TagValueDocument implements Closeable {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String NO_ASSERTION = "NOASSERTION";

    private final Writer writer;

    /**
     * Starts a new tag-value document.
     */
    public TagValueDocument(OutputStream stream) {
        writer = new OutputStreamWriter(stream);
    }

    /**
     * Writes a tag with a (single line) plain value.
     */
    public void addValue(String tag, @NullOr Object value) throws IOException {
        if (value instanceof Optional) {
            value = ((Optional<?>) value).orElse(null);
        }

        if (value == null) {
            value = NO_ASSERTION;
        }

        final var string = value.toString();
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

