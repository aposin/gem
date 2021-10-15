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
package org.aposin.gem.core.impl.internal.config.prefs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.prefs.IPreferences;
import org.aposin.gem.core.api.config.prefs.PrefConstants;
import org.aposin.gem.core.api.config.prefs.values.IPrefValue;
import org.aposin.gem.core.impl.internal.config.HoconFilesManager;
import org.aposin.gem.core.utils.ExecUtils;

/**
 * Default implementation of the preferences based on HOCON files.
 * </br>
 * In this class is kept the list of added preferences and the core added ones.
 * Managing of the preference file is done by the {@link HoconFilesManager}.
 */
@SuppressWarnings("rawtypes") // required because we work witrh IPrefValue without type
public class PreferencesImpl implements IPreferences, IRefreshable {

    private final HoconFilesManager hoconPrefHandler;

    private final Map<String, IPrefValue> addedPrefs = new LinkedHashMap<>();
    private GitBinaryPref gitBinaryPref;

    public PreferencesImpl(final HoconFilesManager hoconPrefHandler) {
        this.hoconPrefHandler = hoconPrefHandler;
    }

    @Override
    public Path getGitBinary() {
        return getGitBinaryPref().getValue();
    }

    private GitBinaryPref getGitBinaryPref() {
        if (gitBinaryPref == null) {
            gitBinaryPref = (GitBinaryPref) hoconPrefHandler.updatePrefValue(new GitBinaryPref());
            if (gitBinaryPref.getValue() == null) {
                gitBinaryPref.setValue(ExecUtils.findExecutable("git"));
            }
        }
        // always update (might have change on the configuration by other means)
        return (GitBinaryPref) hoconPrefHandler.updatePrefValue(gitBinaryPref);
    }

    public boolean has(final String id) {
        return PrefConstants.GIT_ID.equals(id) || addedPrefs.containsKey(id);
    }

    @Override
    public IPrefValue get(final String id) throws GemConfigurationException {
        // handle first the core preferences
        if (PrefConstants.GIT_ID.equals(id)) {
            return getGitBinaryPref();
        }
        IPrefValue prefValue = addedPrefs.get(id);
        if (prefValue != null) {
            return hoconPrefHandler.updatePrefValue(prefValue);
        }
        throw new GemException("Unknown preference " + id);
    }

    @Override
    public IPrefValue getOrSetDefault(final String id, Supplier<IPrefValue> defaultSupplier) {
        // default is already set on the core preferences by the framework
        if (PrefConstants.GIT_ID.equals(id)) {
            return getGitBinaryPref();
        }
        return addedPrefs.computeIfAbsent(id, key -> defaultSupplier.get());
    }

    public List<IPrefValue> getAll() {
        final List<IPrefValue> prefValues = new ArrayList<>(addedPrefs.size() + 1);
        prefValues.add(getGitBinaryPref());
        addedPrefs.values().stream() //
                .forEach(prefVal -> prefValues.add(hoconPrefHandler.updatePrefValue(prefVal)));
        return prefValues;
    }

    @Override
    public void setDefault(IPrefValue value) throws GemConfigurationException {
        if (addedPrefs.putIfAbsent(value.getId(), value) != null) {
            throw new GemException("Trying to set a duplicate default preference: " + value.getId());
        }
    }

    @Override
    public void registerToPersist(String id) throws GemConfigurationException {
        hoconPrefHandler.registerToPersist(get(id));
    }

    @Override
    public void persist() throws GemException {
        hoconPrefHandler.persistPrefs();
    }

    @Override
    public <T> T getPreferenceBean(String id, Class<T> prefBean) throws GemConfigurationException {
        return hoconPrefHandler.getPreferenceBean(id, prefBean);
    }

    @Override
    public void refresh() {
        // this should only be called by the ConfigurationImpl so no need to refresh also the HoconFileManager
        this.gitBinaryPref = null;
        this.addedPrefs.clear();
    }

}