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
package org.aposin.gem.core.api.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.aposin.gem.core.api.workflow.IEnvironmentWorkflow;

/**
 * Represents an environment.
 */
public interface IEnvironment extends INamedObject, IConfigurable {

    /**
     * Separator for methods constructing branch names.
     */
    public static final String BRANCH_NAME_SEPARATOR = "/";

    /**
     * Folder name for worktrees.
     */
    public static final String WORKTREES_FOLDER_NAME = "worktrees";

    /**
     * Branch prefix for internal branches (GEM-specific).
     */
    public static final String INTERNAL_BRANCH_PREFIX = "internal/GEM/";

    /**
     * Separator used by default implementation of some methods
     * in the interface.
     */
    public static final String DEFAULT_SEPARATOR = "_";

    /**
     * Gets the ID of the environment.
     * </br>
     * The ID <em>must</em> contain information from the project,
     * to be easily differentiated for other project's similar
     * environments.
     * Default implementation joins with {@link #DEFAULT_SEPARATOR}
     * the ID from the project and the environment name.
     * 
     * @return id
     */
    @Override
    public default String getId() {
        return getProject().getId() + DEFAULT_SEPARATOR + getName();
    }

    /**
     * Gets the internal branch prefix to use when setting up environment
     * worktrees.
     * </br>
     * IMPORTANT: this internal branch should always be prefixed by
     * {@link #INTERNAL_BRANCH_PREFIX}; otherwise, the core would fail to
     * identify internal branches.
     * 
     * @return {@link #INTERNAL_BRANCH_PREFIX} followed by {@link #getBranchPrefix()}.
     * @nooverride
     */
    public default String getGemInternalBranchName() {
        return INTERNAL_BRANCH_PREFIX + getBranchPrefix();
    }

    /**
     * Gets the prefix for the environment branches.
     * </br>
     * IMPORTANT: this branch prefix should always be prefixed by
     * {@link IProject#getBranchPrefix()} for the project where this
     * environment is attached to; otherwise, the core would fail to
     * identify internal branches.
     * </br>
     * Default implementation returns a path-like prefix consisting on
     * {@link #getProject()#getBranchPrefix()}/{@link #getName()}.
     * 
     * @return prefix for identifying a branch created by GEM for this environment.
     */
    public default String getBranchPrefix() {
        return getProject().getBranchPrefix() + BRANCH_NAME_SEPARATOR + getName();
    }

    /**
     * Gets the name of the environment.
     * </br>
     * This name could be shared by different environments;
     * thus, the ID might be the same or might differ.
     * 
     * @return name the 
     */
    @Override
    public String getName();

    /**
     * Gets the display name of the environment.
     * 
     * @return display name.
     */
    @Override
    public String getDisplayName();

    /**
     * Gets the project where this environment belongs to.
     * 
     * @return enclosing project.
     */
    public IProject getProject();

    /**
     * Gets a list of the repositories defined in this environment.
     * 
     * @return list of repositories.
     */
    public List<IRepository> getRepositories();

    /**
     * Gets a list of the environment's work-trees.
     * </br>
     * This worktrees are assumed to be located on the {@link #getEnvironmentWorktreesLocation()}.
     * 
     * @return list of work-trees.
     */
    public List<IWorktreeDefinition> getEnvironmentWorktrees();

    /**
     * Gets the location where all the environment worktrees should be located.
     * </br>
     * Default implementation returns {@link #WORKTREES_FOLDER_NAME}/{@link #getId()}
     * on the {@link IConfiguration#getResourcesDirectory()}.
     * 
     * @return path to the worktrees.
     */
    public default Path getWorktreesBaseLocation() {
        return getConfiguration().getResourcesDirectory()//
                .resolve(WORKTREES_FOLDER_NAME).resolve(getId());
    }

    /**
     * Gets the mapping between repository information and the branch
     * for this environment.
     * 
     * @return mapping of repository and environment branch.
     * @see #getRepositories()
     */
    public Map<IRepository, String> getEnvironmentBranchByRepository();

    /**
     * Gets the environment branch for the repository.
     * 
     * @param repository environment branch for the repository.
     * @return same result as {@code getEnvironmentBranchByRepository().get(repository)}.
     */
    public default String getEnvironmentBranch(final IRepository repository) {
        return getEnvironmentBranchByRepository().get(repository);
    }

    /**
     * Gets the workflow for this environment.
     * 
     * @return workflow the workflow for this environment.
     */
    public IEnvironmentWorkflow getWorkflow();

    /**
     * Gets the launcher for this environmet.
     * </br>
     * Default implementation gets the launchers from the registered
     * {@link IEnvironmentLauncherProvider#getLaunchers(IEnvironment)},
     * which should always being returned.
     * Additional launchers could be configured at the environment level.
     * 
     * @return launchers; empty list if none.
     */
    public default List<ILauncher> getLaunchers() {
        return getConfiguration().getServiceContainer()//
                .getEnvironmentLauncherProviders().stream() //
                .flatMap(provider -> provider.getLaunchers(this).stream()) //
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the launchers for the repositories in the environment.
     * </br>
     * Default implementation gets the launchers from the registered
     * {@link IEnvironmentLauncherProvider#getRepositoryLaunchers(IRepository)},
     * which should always being returned.
     * Additional launchers could be configured at the environment level.
     * 
     * @return launchers; empty list if none.
     */
    public default List<ILauncher> getRepositoryLaunchers(final IRepository repository) {
        return getConfiguration().getServiceContainer()//
                .getEnvironmentLauncherProviders().stream() //
                .flatMap(provider -> { //
                    final List<ILauncher> launchers = provider.getRepositoryLaunchers(this).get(repository); //
                    return launchers != null ? launchers.stream() : Stream.empty();
                }) //
                .collect(Collectors.toList());
    }

}
