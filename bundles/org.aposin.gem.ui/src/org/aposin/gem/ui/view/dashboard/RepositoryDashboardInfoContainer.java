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

import java.util.List;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.ui.process.LocalRepoStatus;

/**
 * Abstract common class representing the information for the repository
 * dashboard.
 */
public abstract class RepositoryDashboardInfoContainer {

    private final IEnvironment environment;
    private final int repoIndex;
    
    // computed values
    private LocalRepoStatus status = LocalRepoStatus.PLACEHOLDER;
    
    /**
     * Constructor for the container.
     * 
     * @param environment
     * @param repoIndex
     */
    protected RepositoryDashboardInfoContainer(final IEnvironment environment, final int repoIndex) {
        this.environment = environment;
        this.repoIndex = repoIndex;
    }
    
    protected final IEnvironment getEnvironment() {
        return environment;
    }
    
    protected final IRepository getRepository() {
        return environment.getRepositories().get(repoIndex);
    }
    
    protected final IWorktreeDefinition getWorktree() {
        return environment.getEnvironmentWorktrees().get(repoIndex);
    }
    
    public final String getRepositoryName() {
        return getRepository().getId();
    }

    public final String getBaseBranch() {
        return environment.getEnvironmentBranch(getRepository());
    }
    
    public final LocalRepoStatus getStatus() {
        return status;
    }
    
    public final LocalRepoStatus computeStatus() {
        this.status = computeRepoStatus();
        return status;
    }
    
    protected abstract LocalRepoStatus computeRepoStatus();
    
    /**
     * Gets the repository launchers for the contained repo.
     * 
     * @return list of launchers.
     */
    public abstract List<ILauncher> getRepositoryLaunchers();
    
}
