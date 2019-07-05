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
package org.aposin.gem.ui.view.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.process.LocalRepoStatus;
import org.aposin.gem.ui.process.launcher.CopyToClipboardLauncher;

/**
 * Dashboard information for the feature-branches part.
 */
public class FeatureBranchesDashboardInfo extends EnvironmentDashboardInfo {
    
    private final IFeatureBranch featureBranch;

    public FeatureBranchesDashboardInfo(final IFeatureBranch featureBranch, final IEnvironment environment, final int repoIndex) {
        super(environment, repoIndex);
        this.featureBranch = featureBranch;
    }
    
    public String getDashboardDescriptionName() {
        final String worktreeBranch = getWorktreeBranch();
        if (worktreeBranch != null) {
            return getRepositoryName() + "@" + worktreeBranch;
        }
        return getRepositoryName();
    }
    
    public String getTargetBranchName() {
        String targetBranchDisplayName = getTargetBranch();
        if (targetBranchDisplayName != null
                && targetBranchDisplayName.startsWith(getEnvironment().getGemInternalBranchName())) {
            targetBranchDisplayName = featureBranch.getName() + " (internal)";
        }
        return targetBranchDisplayName;
    }
    
    protected String getTargetBranch() {
        return featureBranch == null ? null
                : featureBranch.getCheckoutBranch(getWorktree().getRepository());
    }
    
    public String getWorktreeBranch() {
        final LocalRepoStatus status = getStatus();
        String worktreeBranch = null;
        if (status != null) {
            switch (getStatus()) {
                case READY:
                case REQUIRES_FB_CHECKOUT:
                    worktreeBranch = getWorktree().getBranch();
                    // do not show the internal branch name!
                    if (Objects.equals(worktreeBranch,
                            getEnvironment().getGemInternalBranchName())) {
                        return getEnvironment().getEnvironmentBranch(getRepository());
                    }
                    break;
                case REQUIRES_CLONE:
                case REQUIRES_WORKTREE:
                case PLACEHOLDER:
                case NOT_AVAILABLE:
                default:
                    worktreeBranch = null;
            }
        }
        return worktreeBranch;
    }

    @Override
    protected LocalRepoStatus computeRepoStatus() {
        final LocalRepoStatus status = super.computeRepoStatus();
        if (status == LocalRepoStatus.READY) {
            if (!Objects.equals(getWorktree().getBranch(), getTargetBranch())) {
                return LocalRepoStatus.REQUIRES_FB_CHECKOUT;
            }
        }
        return status;
    }
    
    @Override
    public List<ILauncher> getRepositoryLaunchers() {
        final List<ILauncher> launchers = new ArrayList<>(super.getRepositoryLaunchers());
        if (featureBranch != null) {
            launchers.addAll(featureBranch.getRepositoryLaunchers(getRepository()));
        }
        launchers.add(new CopyToClipboardLauncher(getWorktree(), this::getTargetBranchName, "Target Branch"));
        return launchers;
    }
    
}
