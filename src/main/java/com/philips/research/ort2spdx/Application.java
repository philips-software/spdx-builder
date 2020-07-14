package com.philips.research.ort2spdx;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

public class Application {
    public static void main(String... args) {
        System.exit(new CommandLine(new Run()).execute(args));
    }
}

@Command(name = "ort2spdx", mixinStandardHelpOptions = true, version = "1.0")
class Run implements Callable<Integer> {
    @CommandLine.Option(names = "-x")
    int x;

    public Integer call() { // business logic
        System.out.printf("x=%s%n", x);
        return 123; // exit code
    }
}
