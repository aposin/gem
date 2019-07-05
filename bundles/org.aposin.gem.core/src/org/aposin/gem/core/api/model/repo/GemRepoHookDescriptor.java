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
package org.aposin.gem.core.api.model.repo;

import java.net.URI;

/**
 * Interface for a GEM git-hook description.
 * </br>
 * This interface represents an installable client git-hook
 * that could be installed via GEM.
 */
public interface GemRepoHookDescriptor {

    /**
     * Install scope for the GEM git-hooks.
     * </br>
     * Only this hooks are installable by GEM.
     */
    public enum InstallScope {
        GIT_PRE_COMMIT("pre-commit"), //
        GIT_PRE_PUSH("pre-push"), //
        GIT_PRE_REBASE("pre-rebase");

        private final String scriptName;

        InstallScope(final String name) {
            this.scriptName = name;
        }

        /**
         * Gets the script name for the scope.
         * 
         * @return script name.
         */
        public String getScriptName() {
            return scriptName;
        }

        /**
         * Gets the script directory name for the scope.
         * 
         * @return script directory.
         */
        public String getScriptDirectoryName() {
            return scriptName + ".d";
        }

    }

    /**
     * Path where this git-hook script is located.
     * 
     * @return the script path.
     */
    // TODO: change for java.nio.Path if possible
    // TODO: not possible with classresources in OSGI
    public URI getScriptLocation();

    /**
     * Gets the scopes where this git-hook should be installed.
     * 
     * @return scopes.
     */
    public InstallScope[] getInstallScope();

}
