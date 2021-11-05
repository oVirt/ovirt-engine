/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.rsdl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class is a simple utility that extracts the XML schema and the RSDL files from the engine artifacts and saves
 * it to a directory. It will be typically invoked using the {@code exec-maven-plugin} and with a command line like
 * this:
 *
 * <pre>
 * rsdl-io-manager --version=4 /tmp/myfiles
 * </pre>
 *
 * The {@code version} option is new in version 4 of the engine, so in order to preserve backwards compatibility when
 * it isn't given the default is 3.
 */
public class RsdlIOManager {
    // Names of the command line options:
    private static final String API_VERSION_OPTION = "api-version";

    // Default values of the command line options:
    private static final String API_VERSION_DEFAULT = "3";

    // The name of the files containing the names of the resources to copy:
    private static final String[] RESOURCE_NAMES = {
        "api.xsd",
        "rsdl.xml",
        "rsdl_gluster.xml",
    };

    public static void main(String[] args) throws IOException {
        // Create the command line options:
        Options options = new Options();

        // Option for the API version:
        options.addOption(Option.builder()
            .longOpt(API_VERSION_OPTION)
            .desc("The version number of the API, the default is version 3.")
            .required(false)
            .hasArg(true)
            .argName("VERSION")
            .build()
        );

        // Parse the command line:
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException exception) {
            System.err.println(exception.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.setSyntaxPrefix("Usage: ");
            formatter.printHelp("rsdl-io-manager [OPTIONS] DIRECTORY", options);
            System.exit(1);
        }

        // Extract the API version:
        String apiVersion = line.getOptionValue(API_VERSION_OPTION);
        if (apiVersion == null || apiVersion.isEmpty()) {
            apiVersion = API_VERSION_DEFAULT;
        }

        // Check the arguments:
        args = line.getArgs();
        if (args.length != 1) {
            System.err.println("Exactly one argument containing the output directory is required.");
            System.exit(1);
        }

        // Make sure that the output directory exists:
        File outputDirectory = new File(args[0]);
        Files.createDirectories(outputDirectory.toPath());

        // Copy the resources to the output directory:
        for (String resourceName : RESOURCE_NAMES) {
            exportResource(apiVersion, resourceName, outputDirectory);
        }
    }

    private static void exportResource(String apiVersion, String resourceName, File outputDirectory) throws IOException {
        // Calculate the path of the resource:
        String resourcePath = String.format("/v%s/%s", apiVersion, resourceName);

        // Copy the resource to the output directory:
        InputStream inputResource = RsdlIOManager.class.getResourceAsStream(resourcePath);
        File outputFile = new File(outputDirectory, resourceName);
        System.out.printf("Copying resource \"%s\" to file \"%s\"%n", resourcePath, outputFile.getAbsolutePath());
        Files.copy(inputResource, outputFile.toPath());
    }
}
