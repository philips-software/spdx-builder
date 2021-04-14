/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.*;

public class TreeReader implements BomReader {
    private final TreeFormats formats;
    private final String format;
    private final InputStream stream;

    public TreeReader(InputStream stream, String format, @NullOr File extension) {
        formats = new TreeFormats();
        if (extension != null) {
            formats.extend(extension);
        }
        this.format = format;
        this.stream = stream;
    }

    @Override
    public void read(BillOfMaterials bom) {
        try (final var reader = new BufferedReader(new InputStreamReader(stream))) {
            final var parser = new TreeParser(bom);
            formats.configure(parser, format);

            @NullOr String line = reader.readLine();
            while (line != null) {
                parse(parser, line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new TreeException("Failed to read the tree data");
        }
    }

    private void parse(TreeParser parser, String line) {
        try {
            parser.parse(line)
                    .ifPresent(format -> formats.configure(parser.clearFormat(), format));
        } catch (TreeException e) {
            System.err.println(line);
            throw e;
        }
    }
}
