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
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.workflow.IRepositoryCommandBuilder;

/**
 * Common definition for a local repository.
 * </br>
 * This interface shared by repositories that are in the local
 * filesystem and could run git commands on them.
 */
public interface ILocalRepositoryDef extends IConfigurable, INamedObject, IRefreshable {

    /**
     * Gets the ID for the repository (or work-tree).
     * 
     * @return repository/worktree ID
     */
    @Override
    public String getId();

    /**
     * Gets the name of the repository (or work-tree).
     * </br>
     * Default implementation returns the same as the ID.
     * 
     * @return repository/worktree name.
     */
    @Override
    public default String getName() {
        return getId();
    }

    /**
     * Gets the display name of the repository.
     * </br>
     * Default implementation returns the name/ID.
     * 
     * @return display name for the repository.
     */
    @Override
    public default String getDisplayName() {
        return getName();
    }

    /**
     * Gets the destination location for the repository (or work-tree).
     * 
     * @return filesystem path. 
     */
    public Path getDestinationLocation();

    /**
     * Checks if the repository is clean.
     * </br>
     * The repository is considered clean if there is no change
     * that could produce an error when running other commands
     * (e.g., checkout).
     * </br>
     * For example, both tracked/untracked files on the repository
     * marks the repository as dirty, while ignored or empty folders
     * not.
     * 
     * @return {@code true} if the repository is clean; {@code false} otherwise.
     * @throws RepositoryException if there is an error retrieving
     *         the repository branch (implementation-dependent)
     */
    public boolean isClean() throws RepositoryException;

    /**
     * Gets the current branch on the repository/work-tree.
     * </br>
     * If the local repository does not exists, the return
     * value is implementation dependent.
     * 
     * @return branch where this repository is; if not available,
     *         it is implementation dependent.
     * @throws RepositoryException if there is an error retrieving
     *         the repository branch (implementation-dependent)
     */
    public String getBranch() throws RepositoryException;

    /**
     * Gets the branches for the repository.
     * </br>
     * The branches might be prefixed by any ref (e.g., in git the origin/master
     * and master branches are two different ones.
     * 
     * @return list of  branches.
     * @throws RepositoryException if there is an error retrieving
     *         the repository branch (implementation-dependent)
     */
    public List<String> getBranches() throws RepositoryException;

    /**
     * Checks if the repository contains the remote branch.
     * </br>
     * In the git implementation, for example, the remote branch
     * is marked with the ref "origin".
     * 
     * @param branch branch to check.
     * @return {@code true} if it contains the branch; {@code false} otherwise.
     */
    public boolean containRemoteBranch(final String branch);
    
    /**
     * Checks if the repository contains a matching branch.
     * </br>
     * A matching branch could be also, for example, a remote branch
     * which is not yet on the local repository (e.g., origin/branch).
     * 
     * @param branch branch to check.
     * @return {@code true} if it contains the branch; {@code false} otherwise.
     */
    public boolean containMatchingBranch(final String branch);

    /**
     * Gets the command builder for the repository.
     * 
     * @return the workflow for this repository.
     */
    public IRepositoryCommandBuilder getCommandBuilder();

}
