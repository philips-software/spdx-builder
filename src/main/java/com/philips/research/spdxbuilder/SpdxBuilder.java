/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder;

import com.philips.research.spdxbuilder.controller.BlackDuckCommand;
import com.philips.research.spdxbuilder.controller.OrtCommand;
import com.philips.research.spdxbuilder.controller.TreeCommand;
import com.philips.research.spdxbuilder.core.BusinessException;
import picocli.CommandLine;

public class SpdxBuilder {
    public static void main(String... args) {
        final var cmd = new CommandLine(new Runner());
        try {
            cmd.setExecutionExceptionHandler(SpdxBuilder::exceptionHandler);
            cmd.execute(args);
        } catch (BusinessException e) {
            printError(cmd, "Conversion failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int exceptionHandler(Exception e, CommandLine cmd, CommandLine.ParseResult parseResult) {
        if (e instanceof BusinessException) {
            printError(cmd, "Conversion failed: " + e.getMessage());
            return 1;
        }

        printError(cmd, "An internal error occurred: " + e);
        e.printStackTrace();
        return 1;
    }

    private static void printError(CommandLine cmd, String message) {
        cmd.getErr().println(cmd.getColorScheme().errorText(message));
    }


    @CommandLine.Command(subcommands = {OrtCommand.class, TreeCommand.class, BlackDuckCommand.class},
            description = "Builds SPDX bill-of-materials files from various sources")
    static class Runner {
    }
}

