/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.domain.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.blackduck.BlackDuckReader;
import com.philips.research.spdxbuilder.persistence.spdx.SpdxWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URL;

/**
 * CLI command to export the SBOM from Black Duck to an SPDX file.
 */
@Command(name = "blackduck", aliases = {"bd"}, description = "Extracts a bill-of-materials from a project version in Synoptic Black Duck.")
public class BlackDuckCommand extends AbstractCommand {
    @Parameters(index = "0", description = "Project name")
    String project;

    @Parameters(index = "1", description = "Version")
    String version;

    @Option(names = {"--url"}, description = "Black Duck server URL (defaults to BLACKDUCK_URL environment variable)",
            defaultValue = "${env:BLACKDUCK_URL}", required = true)
    URL url;

    @Option(names = {"--insecure"}, description = "Disable SSL certificate checking")
    boolean insecure = false;

    @Option(names = {"--token"}, description = "Black Duck authorization token (defaults to BLACKDUCK_API_TOKEN environment variable)",
            defaultValue = "${env:BLACKDUCK_API_TOKEN}", required = true)
    String token;

    @Override
    protected ConversionService createService() {
        final BomReader reader = new BlackDuckReader(url, token, project, version, insecure);
        final BomWriter writer = new SpdxWriter(spdxFile);

        return new ConversionInteractor(reader, writer);
    }
}
