/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.domain.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
import com.philips.research.spdxbuilder.persistence.tree.TreeReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * CLI command to export the SBOM from a textual tree representation to an SPDX file.
 */
@Command(name = "tree", description = "Converts the package tree from your build tool into a bill-of-materials.")
public class TreeCommand extends AbstractCommand {
    @CommandLine.Option(names = {"-f", "--format"}, description = "Format of the tree to parse", required = true)
    String format;

    @Override
    protected ConversionService createService() {
        final BomReader reader = new TreeReader(format);
        final BomWriter writer = new SpdxWriter(spdxFile);

        return new ConversionInteractor(reader, writer);
    }
}
