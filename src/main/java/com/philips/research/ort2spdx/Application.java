package com.philips.research.ort2spdx;

import com.philips.research.ort2spdx.controller.RunCommand;
import picocli.CommandLine;

public class Application {
    public static void main(String... args) {
        System.exit(new CommandLine(new RunCommand()).execute(args));
    }
}

