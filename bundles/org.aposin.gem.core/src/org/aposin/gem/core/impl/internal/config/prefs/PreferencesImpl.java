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
import java.nio.file.Paths;

import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.ConfigConstants;
import org.aposin.gem.core.api.config.prefs.IPreferences;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.core.impl.internal.config.HoconFilesManager;
import org.aposin.gem.core.impl.internal.config.bean.GemPrefsBean;
import org.aposin.gem.core.utils.ExecUtils;

public class PreferencesImpl implements IPreferences, IRefreshable {

    // default values cached at the class level to avoid re-computing (e.g., path-finding)
    private static Path defaultGitPath;

    private final HoconFilesManager hoconFileManager;
    private GemPrefsBean prefsBean;

    public PreferencesImpl(final HoconFilesManager hoconFileManager) {
        this.hoconFileManager = hoconFileManager;
    }

    /**
     * Gets the git binary.
     * 
     * @return
     */
    public Path getGitBinary() {
        if (getPrefsBean().binaries.git != null) {
            return Paths.get(prefsBean.binaries.git);
        }
        if (defaultGitPath == null) {
            // set default if not present
            final Path gitPath = ExecUtils.findExecutable("git").toAbsolutePath();
            defaultGitPath = gitPath;
        }
        return defaultGitPath;
    }

    /**
     * Sets the git binary.
     * 
     * @param binary
     */
    public void setGitBinary(final Path binary) {
        getPrefsBean().binaries.git = binary.toAbsolutePath().toString();
    }

    @Override
    public Path getPreferencesPath() {
        return hoconFileManager.getConfigFileProvider().getPrefFile();
    }

    @Override
    public void persist() throws GemException {
        hoconFileManager.persistPrefs();
    }

    private GemPrefsBean getPrefsBean() {
        if (prefsBean == null) {
            prefsBean = hoconFileManager.getPreferenceBean(ConfigConstants.GEM_PREFERENCES_ID, GemPrefsBean.class);
        }
        return prefsBean;
    }

    @Override
    public void refresh() {
        prefsBean = null;
    }

}
