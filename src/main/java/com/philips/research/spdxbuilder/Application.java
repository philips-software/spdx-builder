package com.philips.research.spdxbuilder;

import com.philips.research.spdxbuilder.controller.ConvertCommand;
import com.philips.research.spdxbuilder.core.ConversionInteractor;
import com.philips.research.spdxbuilder.persistence.ConversionPersistence;
import picocli.CommandLine;

public class Application {
    public static void main(String... args) {
        try {
            final var store = new ConversionPersistence();
            final var service = new ConversionInteractor(store);

            new CommandLine(new ConvertCommand(service)).execute(args);
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
            System.exit(1);
        }
    }
}

