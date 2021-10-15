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
import java.util.List;
import java.util.function.Supplier;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.prefs.values.IPrefValue;

/**
 * Preferences for GEM, which could be persisted and loaded.
 * </br>
 * The concrete methods represents the values from the core.
 */
@SuppressWarnings("rawtypes") // required because we work witrh IPrefValue without type
public interface IPreferences {

    /**
     * Gets the git binary path
     * Checks if the key is present on the preferences.
     * 
     * @return path to git command; {@code null} if not found.
     * @param id the ID for the preference
     * @return {@code true} if it is present; {@code false} otherwise
     */
    public Path getGitBinary();

    public boolean has(final String id);

    public IPrefValue get(final String id) throws GemConfigurationException;

    public IPrefValue getOrSetDefault(final String id, final Supplier<IPrefValue> defaultSupplier);

    /**
     * Gets a view of the all the default preferences and core ones.
     * </br>
     * Setting the value on the {@link IPrefValue} won't be persisted
     * until {@link #registerToPersist(IPrefValue)} are called.
     * 
     * @return a view of all preferences
     */
    // TODO: maybe join by group?
    public List<IPrefValue> getAll();

    /**
     * Sets the default preference value.
     * </br>
     * Default preferences are not registered to be persisted
     * until {@link #registerToPersist(IPrefValue)} is called for the same.
     * 
     * @param value default preference value
     * 
     * @throws GemConfigurationException if the preference cannot be set.
     */
    public void setDefault(final IPrefValue value) throws GemConfigurationException;

    /**
     * Sets the default preferences.
     * </br>
     * Default preferences are not registered to be persisted
     * until {@link #registerToPersist(IPrefValue)} is called for the same.
     * 
     * @param values default values; will be updated after calling.
     */
    public default void setDefault(final List<IPrefValue> values) {
        for (final IPrefValue v : values) {
            setDefault(v);
        }
    }

    /**
     * Register a preference value to be persisted.
     * </br>
     * The actual persist is not triggered untill {@link #persist()}
     * is called.
     * 
     * @param value the value to be persisted.
     * 
     * @throws GemConfigurationException if the preference cannot be registered.
     */
    public void registerToPersist(final String id) throws GemConfigurationException;

    /**
     * Persists the preferences.
     * 
     * @throws GemException if an error occurs when persisted.
     */
    public void persist() throws GemException;

    /**
     * Gets the preference as a bean.
     * </br>
     * Synchronization of the bean with the preferences object is not done,
     * and modification on the returned object are not persisted on
     * {@link #persist()}.
     * 
     * @param id identifier for the preference bean.
     * @param prefBean object representing the configured options
     * 
     * @return bean with the loaded configuration.
     * @throws GemConfigurationException if the preference is missing or invalid.
     */
    // TODO: how to get the configuration resolved with the preferences?
    // TODO: maybe add a method on the IConfiguration instead?
    public <T> T getPreferenceBean(final String id, final Class<T> prefBean) throws GemConfigurationException;

}