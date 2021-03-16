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
import java.util.Map;

public class TreeReader implements BomReader {
    @Override
    public void read(BillOfMaterials bom) {
        try (final var reader = new BufferedReader(new InputStreamReader(System.in))) {
            final var parser = new TreeParser(bom)
                    //TODO Just temporary fix to see if it works...
                    .withTypes(Map.of("","maven"))
                    .withStartSection("^compileClasspath")
                    .withEndSection("^\\s*$");

            @NullOr String line = "";
            while (line != null) {
                parser.parse(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            //TODO Proper exception handling
            throw new TreeException("Ehhh...");
        }
    }
}
