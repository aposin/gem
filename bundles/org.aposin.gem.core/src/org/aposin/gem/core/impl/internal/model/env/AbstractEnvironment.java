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
package org.aposin.gem.core.impl.internal.model.env;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;

abstract class AbstractEnvironment implements IEnvironment {

    private final IConfiguration config;
    private final IProject project;

    /**
     * 
     * @param config
     * @param project
     * @param environment
     */
    protected AbstractEnvironment(final IConfiguration config, final IProject project) {
        this.config = config;
        this.project = project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IConfiguration getConfiguration() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IProject getProject() {
        return project;
    }

    @Override
    public final List<IWorktreeDefinition> getEnvironmentWorktrees() {
        final List<IRepository> repositories = getRepositories();
        final List<IWorktreeDefinition> worktrees = new ArrayList<>(repositories.size());
        for (final IRepository repo : repositories) {
            final Path internalWorktreeLocation = getWorktreesBaseLocation().resolve(repo.getId());
            final IWorktreeDefinition worktree = repo.getWorktree(internalWorktreeLocation, //
                    this::getGemInternalBranchName); // work-tree is in the internal branch
            worktrees.add(worktree);
        }
        return worktrees;
    }

}
