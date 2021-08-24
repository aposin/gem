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
package org.aposin.gem.core.impl.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.IPreferences;
import org.aposin.gem.core.impl.internal.config.bean.GemPrefsBean;
import org.aposin.gem.core.utils.ExecUtils;

public class PreferencesImpl implements IPreferences {

    // default values cached at the class level to avoid re-computing (e.g., path-finding)
    private static Path defaultGitPath;

    private final Path preferencesPath;
    private final GemPrefsBean prefsBean;

    public PreferencesImpl(final GemPrefsBean prefsBean) {
        this(null, prefsBean);
    }

    public PreferencesImpl(final Path preferencesPath, final GemPrefsBean prefsBean) {
        this.preferencesPath = preferencesPath == null ? getDefaultPath() : preferencesPath;
        this.prefsBean = prefsBean;
    }

    private static final Path getDefaultPath() {
        return Paths.get(System.getProperty("user.home"), "GEM", "prefs", "gem-prefs.config");
    }

    /**
     * Gets the git binary.
     * 
     * @return
     */
    public Path getGitBinary() {
        if (prefsBean.binaries.git != null) {
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
        prefsBean.binaries.git = binary.toAbsolutePath().toString();
    }

    @Override
    public Path getPreferencesPath() {
        return null;
    }

    @Override
    public void persist() throws GemException {
        System.err.println("Should persist to " + preferencesPath);
        // TODO - implement persist file using a renderer in either JSON/properties (depending on
        // the extension)
    }

}
