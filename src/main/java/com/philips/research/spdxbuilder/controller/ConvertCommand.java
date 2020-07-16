package com.philips.research.spdxbuilder.controller;

import com.philips.research.spdxbuilder.core.ConversionService;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;

@CommandLine.Command(name = "ort2spdx", mixinStandardHelpOptions = true, version = "1.0")
public
class ConvertCommand implements Runnable {
    private final ConversionService service;

    @Option(names = "--ort", description = "Read ORT Analyzer YAML file", descriptionKey = "file")
    File ortFile;

    File spdxFile = new File("bom.spdx");

    public ConvertCommand(ConversionService service)  {
        this.service = service;
    }

    @Override
    public void run() {
        if (ortFile != null) {
            service.readOrtAnalysis(ortFile);
        }
        service.writeBillOfMaterials(spdxFile);
    }
}
