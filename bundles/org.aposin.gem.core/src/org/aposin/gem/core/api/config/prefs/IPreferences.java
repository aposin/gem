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
package org.aposin.gem.core.api.config.prefs;

import java.nio.file.Path;

import org.aposin.gem.core.exception.GemException;

/**
 * Preferences for GEM, which could be persisted and loaded.
 */
public interface IPreferences {

    /**
     * Gets the path where this preferences are persisted.
     * 
     * @return path where the preferences are persisted.
     */
    public Path getPreferencesPath();

    /**
     * Persists the preferences.
     * 
     * @throws GemException if an error occurs when persisted.
     */
    public void persist() throws GemException;

    /**
     * Gets the git binary path
     * 
     * @return path to git command; {@code null} if not found.
     */
    public Path getGitBinary();

    /**
     * Sets the git binary.
     * 
     * @param binary path to git command.
     */
    public void setGitBinary(final Path binary);



}
