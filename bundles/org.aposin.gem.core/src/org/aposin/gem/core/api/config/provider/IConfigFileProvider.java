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
package org.aposin.gem.core.api.config.provider;

import java.nio.file.Path;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.prefs.IPreferences;

/**
 * Configuration file provider interface.
 */
public interface IConfigFileProvider {

    /**
     * Default provider, which checks the system properties to configure the provider.
     * </br>
     * This can be used as a delegate in the case that the provider does not find the
     * files.
     */
    public static final IConfigFileProvider DEFAULT = new DefaultConfigFileProvider();

    /**
     * Gets the preference file.
     * 
     * @return preference file.
     */
    // TODO - do not allow null!
    public Path getPrefFile();

    /**
     * Gets the configuration file (updated if necessary).
     * 
     * @param prefs preferences loaded from {@link #getPrefFile()}.
     * 
     * @return configuration file.
     */
    // TODO - do not allow null!
    public Path getConfigFile(final IPreferences prefs);

    /**
     * Gets a path relative to the config file.
     * 
     * @param relativePath relative path.
     * 
     * @return path relative to the config file (not ensured if it exists);
     *         {@code null} if configuration is the reference.
     * @throws GemException if the configuration is not loaded yet.
     */
    public Path getRelativeToConfigFile(final String relativePath) throws GemException;

}
