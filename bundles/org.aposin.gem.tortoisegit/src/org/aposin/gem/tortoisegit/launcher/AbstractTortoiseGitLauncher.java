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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.core.utils.ExecUtils;
import org.aposin.gem.tortoisegit.Activator;
import org.aposin.gem.tortoisegit.TortoiseLauncherProvider;

abstract class AbstractTortoiseGitLauncher implements ILauncher {

    private final TortoiseLauncherProvider group;
    private final IWorktreeDefinition worktree;
    private final boolean addScopeToDisplayName;

    protected AbstractTortoiseGitLauncher(final TortoiseLauncherProvider group,
            final IWorktreeDefinition worktree, final boolean addScopeToDisplayName) {
        this.group = group;
        this.worktree = worktree;
        this.addScopeToDisplayName = addScopeToDisplayName;
    }

    @Override
    public final INamedObject getGroup() {
        return group;
    }

    @Override
    public final String getName() {
        return TortoiseLauncherProvider.LAUNCHER_NAME_PREFIX + getCommandName();
    }

    @Override
    public final String getDisplayName() {
        if (addScopeToDisplayName) {
            return MessageFormat.format("{0} ({1})", //
                    getCommandDisplayName(), //
                    getLaunchScope().getRepository().getId());
        } else {
            return getCommandDisplayName();
        }
    }

    /**
     * Used in {@link #getName()} and default
     * implementation in {@link #getArguments(Path)}.
     * 
     * @return
     */
    public abstract String getCommandName();

    public abstract String getCommandDisplayName();

    @Override
    public final IWorktreeDefinition getLaunchScope() {
        return worktree;
    }

    @Override
    public boolean canLaunch() {
        return worktree.getRepository().isCloned() //
                && worktree.isAdded() //
                && Files.exists(worktree.getDestinationLocation());
    }

    @Override
    public final List<ICommand> launch() throws GemException {
        if (requireParams()) {
            throw new GemException("Params are required!");
        }
        return launchInternal(getExtraArguments(Collections.emptySet()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final List<ICommand> launch(final Set<IParam> params) throws GemException {
        if (!requireParams()) {
            Activator.LOGGER.warn("The command doesn't require params");
        }
        return launchInternal(getExtraArguments(params));
    }

    private final List<ICommand> launchInternal(final List<String> extraArguments) {
        try {
            final List<String> arguments = getCommonArguments();
            arguments.addAll(extraArguments);
            ExecUtils.exec(arguments, null);
        } catch (final IOException e) {
            throw new GemException("Error running TortoiseGit", e);
        }

        return Collections.emptyList();
    }


    /**
     * Default implementation just runds the <code>/command:{@link #getCommandName()}</code>.
     * </br>
     * If extra parameters are needed, this method should be overriden.
     * 
     * @param tortoiseGitProc path to tortoise exe.
     * @return arguments.
     */
    private final List<String> getCommonArguments() {
        final List<String> args = new ArrayList<>(3);
        args.add(group.tortoiseGitProc.toString());
        args.add("/path:\"" + getLaunchScope().getDestinationLocation() + "\"");
        args.add("/command:" + getCommandName());
        return args;
    }

    @SuppressWarnings("rawtypes")
    protected abstract List<String> getExtraArguments(final Set<IParam> params) throws GemException;

}
