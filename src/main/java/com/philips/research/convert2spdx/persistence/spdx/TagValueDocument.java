package com.philips.research.convert2spdx.persistence.spdx;

import java.io.*;
import java.util.Optional;

/**
 * SPDX Tag-value format implementation.
 */
public class TagValueDocument implements Closeable {
    private static final String NO_ASSERTION = "NOASSERTION";

    private final Writer writer;

    /**
     * Starts a new tag-value document.
     *
     * @param stream
     */
    public TagValueDocument(OutputStream stream) {
        writer = new OutputStreamWriter(stream);
    }

    /**
     * Writes a tag with a (single line) plain value.
     *
     * @param tag
     * @param value
     */
    public void addValue(String tag, Object value) throws IOException {
        if ((value == null) || (value instanceof Optional && ((Optional<?>) value).isEmpty())) {
            value = NO_ASSERTION;
        }

        writeLine(tag + ": " + value);
    }

    /**
     * Writes a tag with a (multi line) delimited text value.
     *
     * @param tag
     * @param text
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void addText(String tag, Optional<String> text) throws IOException {
        addText(tag, text.orElse(null));
    }

    /**
     * Writes a tag with a (multi line) delimited text value.
     *
     * @param tag
     * @param text
     */
    public void addText(String tag, String text) throws IOException {
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
     *
     * @param comment
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

