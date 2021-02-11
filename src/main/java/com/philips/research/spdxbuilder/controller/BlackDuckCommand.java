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
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckReader;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
import picocli.CommandLine;

import java.net.URL;

/**
 * CLI command to export the SBOM from Black Duck to an SPDX file.
 */
@CommandLine.Command(name = "blackduck", aliases = {"bd"})
public class BlackDuckCommand extends AbstractCommand {
    @CommandLine.Parameters(index = "0", description = "Project name")
    String project;

    @CommandLine.Parameters(index = "1", description = "Version")
    String version;

    @CommandLine.Option(names = {"--url"}, description = "Black Duck server URL (defaults to BLACKDUCK_URL environment variable)",
            defaultValue = "${env:BLACKDUCK_URL}", required = true)
    URL url;

    @CommandLine.Option(names = {"--token"}, description = "Black Duck authorization token (defaults to BLACKDUCK_API_TOKEN environment variable)",
            defaultValue = "${env:BLACKDUCK_API_TOKEN}", required = true)
    String token;

    @Override
    protected ConversionService createService() {
        final BomReader reader = new BlackDuckReader(url, token, project, version);
        final BomWriter writer = new SpdxWriter(spdxFile);

        return new ConversionInteractor(reader, writer);
    }
}