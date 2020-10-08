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

        writeLine(tag + ": " + value);
    }

    /**
     * Writes a tag with a (multi line) delimited text value.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void addText(String tag, Optional<String> text) throws IOException {
        addText(tag, text.orElse(null));
    }

    /**
     * Writes a tag with a (multi line) delimited text value.
     */
    public void addText(String tag, @NullOr String text) throws IOException {
        addValue(tag, "<text>" + ((text != null) ? text : NO_ASSERTION) + "</text>");
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

