/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder;

import com.philips.research.spdxbuilder.controller.ConvertCommand;
import com.philips.research.spdxbuilder.core.BusinessException;
import picocli.CommandLine;

public class Application {
    public static void main(String... args) {
        try {
            new CommandLine(new ConvertCommand())
                    .setExecutionExceptionHandler(Application::exceptionHandler)
                    .execute(args);
        } catch (BusinessException e) {
            System.err.println("Conversion failed: " + e.getMessage());
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

}

