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
package org.aposin.gem.core.api.config;

import org.aposin.gem.core.api.config.provider.IConfigFileProvider;
import org.aposin.gem.core.impl.internal.config.ConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader for default implementation of GEM configuration.
 */
public class ConfigurationLoader {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);

    private IConfigFileProvider configFileProvider = IConfigFileProvider.DEFAULT;

    /**
     * Builder method to use a custom {@link IConfigFileProvider} instead of the default.
     * 
     * @param provider
     * @return
     */
    public ConfigurationLoader withConfigFileProvider(final IConfigFileProvider provider) {
        this.configFileProvider = provider;
        return this;
    }

    /**
     * Load the configuration with the provided preferences.
     * 
     * @param preferences
     * @return
     */
    public IConfiguration load() {
        LOGGER.info("Loading configuration");
        return new ConfigurationImpl(configFileProvider);
    }
}
