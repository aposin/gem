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

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.ui.process.LocalRepoStatus;
import org.aposin.gem.ui.process.launcher.CopyToClipboardLauncher;

/**
 * Dashboard information for the environment-setup part.
 */
public class EnvironmentDashboardInfo extends RepositoryDashboardInfoContainer {

    public EnvironmentDashboardInfo(final IEnvironment environment, final int repoIndex) {
        super(environment, repoIndex);
    }
    
    public String getWorktreeLocation() {
        return getWorktree().getDestinationLocation().toString();
    }
    
    @Override
    protected LocalRepoStatus computeRepoStatus() {
        if (!getWorktree().getRepository().isCloned()) {
            return LocalRepoStatus.REQUIRES_CLONE;
        } else if (!getWorktree().isAdded()) {
            if (Files.exists(getWorktree().getDestinationLocation())) {
                return LocalRepoStatus.REQUIRES_WORKTREE_REMOVAL;
            } else {
                return LocalRepoStatus.REQUIRES_WORKTREE;
            }
        } else {
            return LocalRepoStatus.READY;
        }
    }

    @Override
    public List<ILauncher> getRepositoryLaunchers() {
        final List<ILauncher> launchers = new ArrayList<>();
        launchers.addAll(getEnvironment().getRepositoryLaunchers(getRepository()));
        // also add the copy-to-clipboard one
        launchers.add(new CopyToClipboardLauncher(getRepository(), this::getBaseBranch, "Base Branch"));
        return launchers;
    }
    
}
