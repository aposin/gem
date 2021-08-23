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
package org.aposin.gem.core.impl.model.repo;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aposin.gem.core.Activator;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;

/**
 * GEM core default hooks.
 */
public final class CoreGemGitHook implements GemRepoHookDescriptor {

    private static final List<CoreGemGitHook> DEFAULTS = Arrays.asList(//
            // iternal branch
            new CoreGemGitHook("internal_branch_check.sh", //
                    InstallScope.GIT_PRE_COMMIT, InstallScope.GIT_PRE_PUSH,
                    InstallScope.GIT_PRE_REBASE)//
    );

    /**
     * Get the default core git-hooks.
     * 
     * @return default core git-hooks.
     */
    public static List<CoreGemGitHook> getDefaults() {
        return Collections.unmodifiableList(DEFAULTS);
    }

    private final String name;
    private final InstallScope[] scopes;

    /**
     * Default constructor.
     * 
     * @param name name of the script on the {@link #CORE_SCRIPT_RESOURCES}.
     * @param scopes git-hook scope.
     */
    private CoreGemGitHook(final String name, final InstallScope... scopes) {
        this.name = name;
        this.scopes = scopes;
    }

    @Override
    public URI getScriptLocation() {
        return Activator.getResource("scripts/githooks/" + name);
    }

    @Override
    public InstallScope[] getInstallScope() {
        return scopes;
    }



}
