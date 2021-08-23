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
package org.aposin.gem.core.impl.internal.model.repo;

import java.nio.file.Path;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.model.RepositoryException;

class WorktreeRepoDefImpl extends AbstractGitRepository implements IWorktreeDefinition {

    // requires to be the implementation as it needs to add data there
    private final RepositoryImpl repo;
    private final Path location;

    // TODO - shouldn't be used except in RepositoryImp
    public WorktreeRepoDefImpl(final RepositoryImpl repo, final Path location,
            final String branch) {
        this.repo = repo;
        this.location = location;
        setBranch(branch);
    }

    @Override
    public IConfiguration getConfiguration() {
        return repo.getConfiguration();
    }

    @Override
    public IRepository getRepository() {
        return repo;
    }

    @Override
    public Path getDestinationLocation() {
        return location;
    }

    @Override
    public boolean isAdded() {
        return repo.getAddedWorktrees().contains(this);
    }

    @Override
    protected void addBranch(final String branch) {
        repo.addBranch(branch);
    }

    @Override
    protected void removeBranch(String branch) {
        repo.removeBranch(branch);
    }

    @Override
    protected void addWorktree(final Path location, final String targetBranch) {
        repo.addWorktree(location, targetBranch);
    }

    @Override
    protected void removeWorktree(final Path location) {
        repo.removeWorktree(location);
    }

    @Override
    protected void refreshBranches() {
        // call the super method!
        repo.refreshBranches();
    }

    @Override
    protected void checkRunRequirements() {
        repo.checkRunRequirements();
        if (!isAdded()) {
            throw new RepositoryException("Cannot perform action if worktree isn't added!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof IWorktreeDefinition) {
            return compareTo((IWorktreeDefinition) obj) == 0;
        }

        return false;
    }
}
