/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder;

import com.philips.research.spdxbuilder.controller.ConvertCommand;
import picocli.CommandLine;

public class Application {
    public static void main(String... args) {
        try {
            new CommandLine(new ConvertCommand()).execute(args);
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
            System.exit(1);
        }
    }
}

