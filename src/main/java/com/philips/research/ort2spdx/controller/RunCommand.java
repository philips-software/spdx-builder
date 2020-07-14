package com.philips.research.ort2spdx.controller;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ort2spdx", mixinStandardHelpOptions = true, version = "1.0")
public
class RunCommand implements Callable<Integer> {
    @CommandLine.Option(names = "-x")
    int x;

    @CommandLine.Parameters(index = "0", description = "ORT (Analyzer) file", descriptionKey = "file")
    File ortFile;

    public Integer call() { // business logic
        System.out.printf("x=%s%n", x);
        return 123; // exit code
    }
}
