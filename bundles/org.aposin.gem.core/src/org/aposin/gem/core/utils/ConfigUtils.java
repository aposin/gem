/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur Foerderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aposin.gem.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.aposin.gem.core.api.config.GemConfigurationException;

import com.typesafe.config.ConfigUtil;

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

    /**
     * Join IDs to retrieve at a concrete path.
     * 
     * @param ids
     * @return
     */
    public static String joinIds(final String... ids) {
        return ConfigUtil.joinPath(ids);
    }
}
