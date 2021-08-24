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
package org.aposin.gem.tortoisegit.launcher;

import java.util.List;
import java.util.Set;

import org.aposin.gem.core.Activator;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.tortoisegit.TortoiseLauncherProvider;

abstract class AbstractNoParamsTortoiseGitLauncher extends AbstractTortoiseGitLauncher {

    protected AbstractNoParamsTortoiseGitLauncher(final TortoiseLauncherProvider group,
            final IWorktreeDefinition worktree, final boolean addScopeToDisplayName) {
        super(group, worktree, addScopeToDisplayName);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final Set<IParam> createParams() throws GemException {
        throw new GemException("No params required for " + getName());
    }

    @Override
    public final boolean requireParams() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected final List<String> getExtraArguments(final Set<IParam> params) {
        if (!params.isEmpty()) {
            Activator.LOGGER.warn("Ignoring params: {}", params);
        }
        return getExtraArguments();
    }

    protected abstract List<String> getExtraArguments();

}
