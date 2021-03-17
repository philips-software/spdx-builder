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

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TreeReader implements BomReader {
    private final TreeFormats formats;
    private final String format;

    public TreeReader(String format) {
        formats = new TreeFormats();
        this.format = format;
    }

    @Override
    public void read(BillOfMaterials bom) {
        try (final var reader = new BufferedReader(new InputStreamReader(System.in))) {
            //TODO Get a configured parser from TreeFormats instance?
            final var parser = new TreeParser(bom);
            formats.configure(parser, format);

            @NullOr String line = "";
            while (line != null) {
                parse(parser, line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new TreeException("Failed to read tree from stdin");
        }
    }

    private void parse(TreeParser parser, String line) {
        try {
            parser.parse(line);
        } catch (TreeException e) {
            System.err.println(line);
            throw e;
        }
    }
}
