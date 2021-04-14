/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
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
        new CommandLine(new Runner())
                .setExecutionExceptionHandler(SpdxBuilder::exceptionHandler)
                .execute(args);
    }

    private static int exceptionHandler(Exception e, CommandLine cmd, CommandLine.ParseResult parseResult) {
        if (e instanceof BusinessException) {
            printError(cmd, "Conversion aborted: " + e.getMessage());
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

