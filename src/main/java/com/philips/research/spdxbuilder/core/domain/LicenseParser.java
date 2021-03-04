/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import pl.tlinkowski.annotation.basic.NullOr;

/**
 * Parser for SPDX-like license statements containing AND, OR, WITH clauses and braces.
 */
public class LicenseParser {
    private final LicenseDictionary dictionary;
    private StringBuilder buffer = new StringBuilder();
    private License license = License.NONE;
    private License current = License.NONE;
    private String identifier = "";
    private Mode mode = Mode.NONE;
    private boolean parsingWith = false;

    private LicenseParser() {
        this(LicenseDictionary.getInstance());
    }

    private LicenseParser(LicenseDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * @return the license matching the provided text
     */
    public static License parse(@NullOr String text) {
        if (text == null || text.isBlank()) {
            return License.NONE;
        }
        //TODO Catch any exceptions and convert to single plain license via dictionary
        return new LicenseParser().decode(text);
    }

    private License decode(String text) {
        for (var i = 0; i < text.length(); i++) {
            final var ch = text.charAt(i);
            switch (ch) {
                case '(':
                    updateCurrent();
                    if (!current.equals(License.NONE)) {
                        appendCurrent();
                    }
                    final var sub = bracketSubstring(text, i + 1);
                    current = new LicenseParser(dictionary).decode(sub);
                    i += sub.length() + 1;
                    break;
                case ')':
                case ' ':
                    addToken();
                    break;
                default:
                    buffer.append(ch);
            }
        }
        addToken();
        appendCurrent();
        return license;
    }

    private String bracketSubstring(String text, int start) {
        var nested = 0;
        for (var i = start; i < text.length(); i++) {
            final var ch = text.charAt(i);
            if (ch == '(') {
                nested++;
            } else if (ch == ')') {
                if (nested == 0) {
                    return text.substring(start, i);
                }
                nested--;
            }
        }
        return text.substring(start);
    }

    private void addToken() {
        final var token = buffer.toString().trim();
        switch (token.toLowerCase()) {
            case "with":
                if (current.equals(License.NONE) && !identifier.isBlank() && !parsingWith) {
                    current = dictionary.licenseFor(identifier);
                    identifier = "";
                    parsingWith = true;
                } else {
                    identifier += ' ' + token;
                }
                break;
            case "and":
                appendCurrent();
                mode = Mode.AND;
                break;
            case "or":
                appendCurrent();
                mode = Mode.OR;
                break;
            default:
                if (!token.isBlank()) {
                    identifier += ' ' + token;
                }
        }
        buffer = new StringBuilder();
    }

    private void appendCurrent() {
        updateCurrent();
        switch (mode) {
            case AND:
                license = license.and(current);
                break;
            case OR:
                license = license.or(current);
                break;
            default:
                if (!current.equals(License.NONE)) {
                    if (license.equals(License.NONE)) {
                        license = current;
                    } else {
                        license = license.and(current);
                    }
                }
        }
        current = License.NONE;
        mode = Mode.NONE;
    }

    private void updateCurrent() {
        if (identifier.isBlank()) {
            parsingWith = false;
            return;
        }
        if (parsingWith) {
            current = dictionary.withException(current, identifier);
            parsingWith = false;
        } else {
            current = dictionary.licenseFor(identifier);
        }
        identifier = "";
    }

    enum Mode {NONE, AND, OR}
}

