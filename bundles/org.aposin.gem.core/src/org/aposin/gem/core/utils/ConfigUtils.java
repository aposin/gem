package org.aposin.gem.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.aposin.gem.core.api.config.GemConfigurationException;

/**
 * Utilities for configuration files.
 */
public class ConfigUtils {

    private ConfigUtils() {
        // cannot be instantiated
    }

    /**
     * Creates an empty configuration file supported by GEM core.
     * 
     * @param file the path to write the file to (should not exists)
     * 
     * @throws IOException if the file cannot be created
     */
    public static void createEmptyConfig(final Path file) throws IOException {
        if (isJson(file) || isHocon(file)) {
            // creates an empty JSON/HOCON file
            Files.write(file, Arrays.asList("{", "", "}"), StandardOpenOption.CREATE_NEW);
        } else {
            throw new GemConfigurationException("Cannot create configuration file - unknown format: " + file);
        }
    }

    /**
     * Checks if the configuration is in the JSON format.
     * 
     * @param file
     * @return
     */
    public static boolean isJson(final Path file) {
        return file.getFileName().toString().endsWith(".json");
    }

    /**
     * Checks if the configuration is in the HOCON format.
     * 
     * @param file
     * 
     * @return
     */
    public static boolean isHocon(final Path file) {
        return file.getFileName().toString().endsWith(".conf");
    }
}
