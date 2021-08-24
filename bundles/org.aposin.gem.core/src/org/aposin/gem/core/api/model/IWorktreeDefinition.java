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

import java.util.List;

/**
 * Represents a repository work-tree definition.
 * </br>
 * This might exists or not in the index or locally.
 */
public interface IWorktreeDefinition extends ILocalRepositoryDef {

    /**
     * Gets the repository for this work-tree.
     * 
     * @return repository for the work-tree.
     */
    public IRepository getRepository();

    /**
     * {@inheritDoc}
     * </br>
     * Default implementation returns the String representation
     * of {@link #getDestinationLocation()} as there is allowed
     * only one worktree per folder.
     */
    public default String getId() {
        return getDestinationLocation().toString();
    }

    /**
     * Gets the branch on the work-tree;
     * if the work-tree does not exists yet or it was removed,
     * returns the configured branch for it.
     * 
     * @return branch where this work-tree is;
     *         defined branch for the work-tree otherwise.
     */
    @Override
    public String getBranch();

    /**
     * {@inheritDoc}
     * </br>
     * Default implementation uses {@code getRepository().getBranches()}.
     * It is discouraged to override this default unless necessary.
     */
    @Override
    public default List<String> getBranches() throws RepositoryException {
        return getRepository().getBranches();
    }

    /**
     * Checks if the repository is added.
     * 
     * @return {@code true} if it is added; {@code false} otherwise.
     */
    public boolean isAdded();

    // TODO - implement and document!
    public default void delete() throws RepositoryException {
        if (!isAdded()) {
            throw new RepositoryException("Cannot delete not added worktree");
        }
        throw new RuntimeException("Not implemented");
    }
}
