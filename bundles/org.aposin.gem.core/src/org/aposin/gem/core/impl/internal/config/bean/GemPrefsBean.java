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
package org.aposin.gem.core.impl.internal.config.bean;

import com.typesafe.config.Optional;

/**
 * Beand for the GEM preferences.
 */
// TODO - create JSON schema and generate with a maven Plug-in?
public final class GemPrefsBean {

    public Binaries binaries;

    public Binaries getBinaries() {
        return binaries;
    }

    public void setBinaries(Binaries binaries) {
        this.binaries = binaries;
    }

    public static class Binaries {

        @Optional
        public String git;

        public String getGit() {
            return git;
        }

        public void setGit(String git) {
            this.git = git;
        }

        @Override
        public String toString() {
            return "Binaries [git=" + git + "]";
        }
    }

    @Override
    public String toString() {
        return "GemPrefsBean [binaries=" + binaries + "]";
    }
}
