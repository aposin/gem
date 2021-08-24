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
package org.aposin.gem.github.service;

import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.github.launcher.OpenBranchLauncher;
import org.aposin.gem.github.launcher.PullRequestLauncher;
import org.osgi.service.component.annotations.Component;

/**
 * Launcher provider implementation for GitHub.
 */
@Component(service = IFeatureBranchLauncherProvider.class)
public class GithubLauncherProvider implements IFeatureBranchLauncherProvider {
    
    /**
     * Name of the provider.
     * </br>
     * It should also be the name of the group for all launchers.
     */
    public static final String NAME = "github";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return "GitHub";
    }

    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        if (!supportsDesktop()) {
            // throws as the only launchers at the moment require browse
            throw new GemConfigurationException("Browse action is not supported");
        }
    }

    public static final boolean supportsDesktop() {
        return Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(IFeatureBranch featureBranch) {
        final Map<IRepository, List<ILauncher>> launchers = featureBranch.getEnvironment() //
                .getRepositories().stream() //
                .collect(Collectors.toMap(r -> r, r -> new ArrayList<>()));
        for (final IWorktreeDefinition worktreee : featureBranch.getEnvironment()
                .getEnvironmentWorktrees()) {
            final IRepository repo = worktreee.getRepository();
            if (isGitHubServer(repo)) {
                final String checkoutBranch = featureBranch.getCheckoutBranch(repo);
                if (!checkoutBranch.startsWith(IEnvironment.INTERNAL_BRANCH_PREFIX)) {
                    launchers.get(repo).add(new PullRequestLauncher(this, worktreee, featureBranch));
                }
            }
        }

        
        return launchers;
    }
    
    @Override
    public List<ILauncher> getLaunchers(final IFeatureBranch featureBranch) {
        final List<ILauncher> launchers = new ArrayList<>();
        for (final IWorktreeDefinition worktreee : featureBranch.getEnvironment()
                .getEnvironmentWorktrees()) {
            final IRepository repo = worktreee.getRepository();
            if (isGitHubServer(repo)) {
                final String checkoutBranch = featureBranch.getCheckoutBranch(repo);
                if (checkoutBranch.startsWith(IEnvironment.INTERNAL_BRANCH_PREFIX)) {
                    launchers.add(new OpenBranchLauncher(this, repo,
                            featureBranch.getEnvironment().getEnvironmentBranch(repo)));
                }
            }
        }

        return launchers;
    }

    private static boolean isGitHubServer(final IRepository repo) {
        // TODO - make patter to identify host as github configurable?
        return repo.getServer().getHost().contains("github");
    }

}
