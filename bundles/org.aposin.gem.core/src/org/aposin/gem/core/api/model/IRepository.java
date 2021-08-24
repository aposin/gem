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

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;
import org.aposin.gem.core.impl.model.repo.CoreGemGitHook;

// TODO - add a way to connect to a remote API to do pull-request
// TODO - and also a way to configure an upstream
public interface IRepository extends ILocalRepositoryDef {

    /**
     * Gets the remote URL for the repository.
     * 
     * @return url as string
     */
    public String getUrl();

    /**
     * Gets the URI for the repository server.
     * 
     * @return repository server URI.
     */
    public URI getServer();

    /**
     * Checks if the repository is cloned.
     * 
     * @return {@code true} if it is cloned; {@code false} otherwise.
     */
    public boolean isCloned() throws RepositoryException;

    /**
     * Gets the current branch where this repository is.
     * 
     * @return branch where this repository is.
     */
    @Override
    public String getBranch() throws RepositoryException;

    /**
     * Gets the set of work-trees defined on the repository.
     * </br>
     * The work-trees might exists or not.
     * 
     * @return list of work-trees
     */
    public List<IWorktreeDefinition> getWorktrees() throws RepositoryException;

    /**
     * Gets the work-tree on the repository corresponding to the provided path.
     * </br>
     * If the path does not exists, then a work-tree definition is
     * created with the branch provided by the branch supplier.
     * 
     * @param worktree
     * @param branchSupplier
     * @return
     */
    public IWorktreeDefinition getWorktree(final Path worktreePath,
            final Supplier<String> branchSupplier);


    /**
     * Gets the hooks for the repository (if any).
     * </br>
     * For git repositories, this should always return the 
     * {@link CoreGemGitHook#getDefaults()} in addition to any
     * implementation-dependent one. 
     * 
     * @return environment hooks to be installed.
     */
    public List<GemRepoHookDescriptor> getHooks();

}
