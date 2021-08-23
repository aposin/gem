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
package org.aposin.gem.tortoisegit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.utils.ExecUtils;
import org.aposin.gem.tortoisegit.launcher.TortoiseGitCommitLauncher;
import org.aposin.gem.tortoisegit.launcher.TortoiseGitLogLauncher;
import org.aposin.gem.tortoisegit.launcher.TortoiseGitStatusLauncher;
import org.osgi.service.component.annotations.Component;

@Component(service = {IFeatureBranchLauncherProvider.class, IEnvironmentLauncherProvider.class})
public class TortoiseLauncherProvider
        implements IFeatureBranchLauncherProvider, IEnvironmentLauncherProvider {

    /**
     * Prefix identifier for tortoise-git launchers.
     */
    public static final String LAUNCHER_NAME_PREFIX = "tortoise_git_";

    public Path tortoiseGitProc;

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public String getDisplayName() {
        return "TortoiseGit";
    }

    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        // TODO - make configurable?
        tortoiseGitProc = ExecUtils.findExecutable("TortoiseGitProc.exe");
        if (tortoiseGitProc == null) {
            throw new GemConfigurationException("TortoiseGitProc.exe not found on the path");
        }
    }

    @Override
    public List<ILauncher> getLaunchers(final IFeatureBranch featureBranch) {
        return Collections.emptyList();
    }
    
    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IFeatureBranch featureBranch) {
        final List<IWorktreeDefinition> worktrees =
                featureBranch.getEnvironment().getEnvironmentWorktrees();
        final Map<IRepository, List<ILauncher>> byRepositoryLaunchers = new LinkedHashMap<>(worktrees.size());
        for (final IWorktreeDefinition wt : worktrees) {
            final String checkoutBranch = featureBranch.getCheckoutBranch(wt.getRepository());
            if (!checkoutBranch.startsWith(IEnvironment.INTERNAL_BRANCH_PREFIX)) {
                final ILauncher launcher = new TortoiseGitCommitLauncher(this, wt, featureBranch, false);
                byRepositoryLaunchers.put(wt.getRepository(), Collections.singletonList(launcher));
            }
        }
        return byRepositoryLaunchers;
    }
    
    @Override
    public List<ILauncher> getLaunchers(final IEnvironment environment) {
        return Collections.emptyList();
    }

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IEnvironment environment) {
        final List<IWorktreeDefinition> worktrees = environment.getEnvironmentWorktrees();
        final Map<IRepository, List<ILauncher>> byRepositoryLaunchers = new LinkedHashMap<>(worktrees.size());
        for (final IWorktreeDefinition wt : worktrees) {
            final List<ILauncher> launchers = new ArrayList<>(2);
            launchers.add(new TortoiseGitLogLauncher(this, wt));
            launchers.add(new TortoiseGitStatusLauncher(this, wt));
            byRepositoryLaunchers.put(wt.getRepository(), launchers);
        }
        return byRepositoryLaunchers;
    }
    
}
