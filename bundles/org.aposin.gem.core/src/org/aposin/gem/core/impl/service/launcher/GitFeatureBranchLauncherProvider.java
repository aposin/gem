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
package org.aposin.gem.core.impl.service.launcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.osgi.service.component.annotations.Component;

/**
 * Provider for launchers related with git-support not
 * in the workflow (e.g., push).
 */
@Component(service = IFeatureBranchLauncherProvider.class)
public class GitFeatureBranchLauncherProvider implements IFeatureBranchLauncherProvider {

    /**
     * Name for the Push launcher.
     */
    public static final String PUSH_LAUNCHER_NAME = "push_feature_branch";

    /**
     * Group for the launchers created by this provider.
     */
    public static final INamedObject GIT_GROUP = new INamedObject() {

        @Override
        public String getName() {
            return "git";
        }

        @Override
        public String getDisplayName() {
            return "Git";
        }
    };
    
    @Override
    public String getName() {
        return GIT_GROUP.getName();
    }

    @Override
    public String getDisplayName() {
        return GIT_GROUP.getDisplayName();
    }

    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        // NO-OP
    }

    @Override
    public List<ILauncher> getLaunchers(final IFeatureBranch featureBranch) {
        return Collections.emptyList();
    }

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IFeatureBranch featureBranch) {
        final List<IWorktreeDefinition> envWorktrees = featureBranch.getEnvironment().getEnvironmentWorktrees();
        final Map<IRepository, List<ILauncher>> launchers = new HashMap<>(envWorktrees.size());
        for (int idx = 0; idx < envWorktrees.size(); idx++) {
            launchers.put(envWorktrees.get(idx).getRepository(), //
                    Collections.singletonList(
                            new GitPushLauncher(featureBranch, idx)));
        }
        return launchers;
    }

    private class GitPushLauncher extends AbstractNoParamsLauncher {

        private final IFeatureBranch featureBranch;
        private final int worktreeIdx;
        
        public GitPushLauncher(final IFeatureBranch featureBranch, final int worktreeIdx) {
            this.featureBranch = featureBranch;
            this.worktreeIdx = worktreeIdx;
        }
        
        @Override
        public String getName() {
            return PUSH_LAUNCHER_NAME;
        }

        @Override
        public String getDisplayName() {
            return "Push";
        }
        
        @Override
        public INamedObject getGroup() {
            return GIT_GROUP;
        }

        @Override
        public IWorktreeDefinition getLaunchScope() {
            return featureBranch.getEnvironment().getEnvironmentWorktrees().get(worktreeIdx);
        }

        @Override
        public boolean canLaunch() {
            final IEnvironment env = featureBranch.getEnvironment();
            return !env.getWorkflow().getCloneLauncher().canLaunch() && //
                    !env.getWorkflow().getSetupWorktreeLauncher().canLaunch() && //
                    !featureBranch.getWorkflow().getFetchAndCheckoutLauncher().canLaunch();
        }

        @Override
        public List<ICommand> launch() throws GemException {
            return Collections.singletonList(getLaunchScope().getCommandBuilder().buildPushCommand());
        }
        
    }
    
}
