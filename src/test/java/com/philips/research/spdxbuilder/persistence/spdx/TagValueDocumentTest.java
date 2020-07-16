/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
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
    private static final String TAG = "Tag";
    private static final String VALUE = "Value";
    private static final String NO_ASSERTION = "NOASSERTION";
    private static final String TEMPLATE = "%s: %s\n";
    private static final String TEXT_TEMPLATE = "%s: <text>%s</text>\n";
    private static final String TAG_VALUE = String.format(TEMPLATE, TAG, VALUE);
    private static final String TAG_TEXT = String.format(TEXT_TEMPLATE, TAG, VALUE);
    private static final String NO_ASSERTION_VALUE = String.format(TEMPLATE, TAG, NO_ASSERTION);
    private static final String NO_ASSERTION_TEXT = String.format(TEXT_TEMPLATE, TAG, NO_ASSERTION);

    private void assertOutput(String expected, DocumentModification test) throws IOException {
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
        final var comment = "My comment";

        assertOutput("## " + comment + "\n", tagValueDocument -> tagValueDocument.addComment(comment));
    }

    @Test
    void writesTagValue() throws Exception {
        assertOutput(TAG_VALUE, (doc) -> doc.addValue(TAG, VALUE));
    }

    @Test
    void writesOptionalTagValue() throws Exception {
        assertOutput(TAG_VALUE, (doc) -> doc.addValue(TAG, Optional.of(VALUE)));
    }

    @Test
    void noAssertionEmptyOptionalTagValue() throws Exception {
        assertOutput(NO_ASSERTION_VALUE, (doc) -> doc.addValue(TAG, Optional.empty()));
    }

    @Test
    void noAssertionNullTagValue() throws Exception {
        assertOutput(NO_ASSERTION_VALUE, (doc) -> doc.addValue(TAG, null));
    }

    @Test
    void writesTextValue() throws Exception {
        assertOutput(TAG_TEXT, (doc) -> doc.addText("Tag", "Value"));
    }

    @Test
    void noAssertionEmptyOptionalTagText() throws Exception {
        assertOutput(NO_ASSERTION_TEXT, (doc) -> doc.addText(TAG, Optional.empty()));
    }

    @Test
    void noAssertionNullTagText() throws Exception {
        assertOutput(NO_ASSERTION_TEXT, (doc) -> doc.addText(TAG, (String) null));
    }
}
