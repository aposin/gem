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
package org.aposin.gem.core.api.workflow;

import java.util.function.Function;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IWorktreeDefinition;

/**
 *  Defines the basic GEM workflow for a feature branch.
 * </br>
 * The workflow <em>must</em> be called in order:
 * <ol>
 *  <li>{@link #checkoutFeatureBranch()} if {@link #requiresCheckout()}</li>
 * </ol>
 * 
 */
public interface IFeatureBranchWorkflow {

    /**
     * Gets the feature-branch where this workflow belongs to.
     * 
     * @return feature-branch
     */
    public IFeatureBranch getFeatureBranch();

    /**
     * Gets the fetch and checkout launcher.
     * </br>
     * This launcher shouldn't be part of the {@link #getLaunchers()}.
     * 
     * @return launcher for fetch and checkout
     */
    public ILauncher getFetchAndCheckoutLauncher() throws WorkflowException;

    /**
     * Merges the base branch into the feature-branch.
     * 
     * @param shouldAbort supplier that informs if the process of merging
     *                    when a conflict is found should continue. Until
     *                    this object does not return {@code true}, merge
     *                    will try to continue indefinitely. 
     * 
     * @return list of commands.
     * @throws WorkflowException if there is any error on the workflow.
     */
    public ILauncher getMergeBaseIntoFeatureBranchLauncher(final Function<IWorktreeDefinition, Boolean> shouldAbort)
            throws WorkflowException;

    /**
     * Pulls the feature-branch from the remote.
     * 
     * @param shouldAbort supplier that informs if the process of merging
     *                    when a conflict is found should continue. Until
     *                    this object does not return {@code true}, merge
     *                    will try to continue indefinitely. 
     * 
     * @return list of commands.
     * @throws WorkflowException if there is any error on the workflow.
     */
    public ILauncher getPullLauncher(final Function<IWorktreeDefinition, Boolean> shouldAbort) throws WorkflowException;

    /**
     * Cleans the worktree if the feature branch is checkout.
     * </br>
     * WARNING: this commands are destructive, and it might delete
     * uncommitted changes.
     * 
     * @return launcher for the worktree cleanup.
     * @throws WorkflowException if there is any error on the workflow.
     */
    public ILauncher getCleanWorktreeLauncher() throws WorkflowException;
    
    /**
     * Removes the local feature-branch.
     *
     * @return launcher
     * @throws WorkflowException if there is any error on the workflow.
     */
    public ILauncher getRemoveBranchLauncher() throws WorkflowException;

}
