/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur FÃ¶rderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.core.api.config.provider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.IPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigFileProvider implements IConfigFileProvider {

    /**
     * Property to set default configuration file for default loader.
     */
    public static final String DEFAULT_CONFIG_FILE_PROPERTY =
            "org.aposin.gem.core.config.config_file";

    /**
     * Property to set default preference file for the loader.
     */
    public static final String DEFAULT_PREF_FILE_PROPERTY = "org.aposin.gem.core.config.pref_file";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigFileProvider.class);

    @Override
    public Path getPrefFile() {
        return loadFromConfig(DEFAULT_PREF_FILE_PROPERTY);
    }

    @Override
    public Path getConfigFile(final IPreferences prefs) {
        return loadFromConfig(DEFAULT_CONFIG_FILE_PROPERTY);
    }

    @Override
    public Path getRelativeToConfigFile(final String relativePath) {
        final Path configFile = loadFromConfig(DEFAULT_CONFIG_FILE_PROPERTY);
        if (configFile == null) {
            throw new GemException("Default configuration does not have sub-paths");
        }
        return configFile.getParent().resolve(relativePath);
    }

    private Path loadFromConfig(final String propertyName) {
        final String fileProperty = System.getProperty(propertyName);
        if (fileProperty == null) {
            LOGGER.warn("System property '{}' not present", propertyName);
            return null;
        }
        final Path asPath = Paths.get(fileProperty);

        if (!Files.exists(asPath)) {
            LOGGER.warn("Configuration file {}={} does not exists.", propertyName, asPath);
            return null;
        }
        return asPath;
    }

}
