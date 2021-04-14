/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@FunctionalInterface
interface DocumentModification {
    void invoke(TagValueDocument doc) throws IOException;
}

class TagValueDocumentTest {
    public static final String COMMENT = "My comment";
    private static final String TAG = "Tag";
    private static final String VALUE = "Value";
    private static final String MULTI_VALUE = "Line1\nLine2";
    private static final String TEMPLATE = "%s: %s\n";
    private static final String MULTI_LINE_TEMPLATE = "%s: <text>%s</text>\n";

    private static void assertOutput(String expected, DocumentModification test) throws IOException {
        final var stream = new ByteArrayOutputStream();
        try (final var doc = new TagValueDocument(stream)) {
            test.invoke(doc);
        } finally {
            assertThat(stream.toString()).isEqualTo(expected);
        }
    }

    @Test
    void writesEmptyLine() throws Exception {
        assertOutput("\n", TagValueDocument::addEmptyLine);
    }

    @Test
    void writesCommentLine() throws Exception {
        assertOutput("## " + COMMENT + "\n", tagValueDocument -> tagValueDocument.addComment(COMMENT));
    }

    @Test
    void writesTagValue() throws Exception {
        assertOutput(String.format(TEMPLATE, TAG, VALUE), (doc) -> doc.addValue(TAG, VALUE));
    }

    @Test
    void writesOptionalTagValue() throws Exception {
        assertOutput(String.format(TEMPLATE, TAG, VALUE), (doc) -> doc.addValue(TAG, Optional.of(VALUE)));
    }

    @Test
    void writesNoAssertion_EmptyOptionalTagValue() throws Exception {
        assertOutput(String.format(TEMPLATE, TAG, "NOASSERTION"), (doc) -> doc.addValue(TAG, Optional.empty()));
    }

    @Test
    void writeNoAssertion_NullTagValue() throws Exception {
        assertOutput(String.format(TEMPLATE, TAG, "NOASSERTION"), (doc) -> doc.addValue(TAG, null));
    }

    @Test
    void writesNone_emptyStringValue() throws Exception {
        assertOutput(String.format(TEMPLATE, TAG, "NONE"), (doc) -> doc.addValue(TAG, ""));
    }

    @Test
    void writesOptionalTag() throws Exception {
        assertOutput(String.format(TEMPLATE, TAG, VALUE), (doc) -> doc.optionallyAddValue(TAG, Optional.of(VALUE)));
    }

    @Test
    void skipsOptionalTag() throws Exception {
        assertOutput("", (doc) -> doc.optionallyAddValue(TAG, Optional.empty()));
    }

    @Test
    void writesTextValue() throws Exception {
        assertOutput(String.format(MULTI_LINE_TEMPLATE, TAG, MULTI_VALUE), (doc) -> doc.addValue(TAG, MULTI_VALUE));
    }
}
