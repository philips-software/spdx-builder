/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.domain.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.bom_base.BomBaseKnowledgeBase;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
import com.philips.research.spdxbuilder.persistence.tree.TreeFormats;
import com.philips.research.spdxbuilder.persistence.tree.TreeReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;

/**
 * CLI command to export the SBOM from a textual tree representation to an SPDX file.
 */
@Command(name = "tree", description = "Converts the package tree from your build tool into a bill-of-materials.")
public class TreeCommand extends AbstractCommand {
    @CommandLine.Option(names = {"-f", "--format"}, description = "Format of the tree to parse")
    @NullOr String format;

    @CommandLine.Option(names = {"--custom"}, description = "Custom formats extension file")
    @NullOr File formatExtension;

    @CommandLine.Option(names = {"--kb", "--bombase"}, description = "Add package metadata from BOM-base knowledge base", paramLabel = "SERVER_URL")
    @NullOr URI bomBase;

    @Override
    public void run() {
        if (format == null) {
            System.err.println("Missing required format specification. Available formats are:\n");
            new TreeFormats().printFormats();
            throw new BusinessException("No tree format specification provided");
        }
        super.run();
    }

    @Override
    protected ConversionService createService() {
        final BomReader reader = new TreeReader(System.in, format, formatExtension);
        final BomWriter writer = new SpdxWriter(spdxFile);

        return bomBase != null
                ? new ConversionInteractor(reader, writer).setKnowledgeBase(new BomBaseKnowledgeBase(bomBase))
                : new ConversionInteractor(reader, writer);
    }
}
