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
package org.aposin.gem.core.api.workflow;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;

/**
 * Defines the basic GEM workflow for a environment.
 * </br>
 * The workflow <em>must</em> be called in order:
 * <ol>
 *  <li>{@link #cloneRepositories()} if {@link #requiresClone()}</li>
 *  <li>{@link #requiresWorktreeSetup()} if {@link #requiresWorktreeSetup()}</li>
 *  <li>{@link #getFeatureBranchWorkflow(IFeatureBranch)} to start/continue a workflow for a feature branch</li>
 * </ol>
 * </br>
 * There are also methods to fetch the repositories or just the environment.
 * 
 * @apiNote Every step of the pipeline returns a list of commands which could be execute in parallel
 * (if more than one is required). 
 */
public interface IEnvironmentWorkflow {

    /**
     * Gets the environment where this workflow belongs to.
     * 
     * @return environment
     */
    public IEnvironment getEnvironment();

    /**
     * Clone the the environment's repositories.
     * </br>
     * IMPORTANT: This is supposed to be done only once.
     * 
     * @return launcher for clone.
     * 
     * @throws WorkflowException
     */
    public ILauncher getCloneLauncher() throws WorkflowException;

    /**
     * Synchronize (fetch/pull) all the environment's branches.
     * </br>
     * This fetches all the branches for the environment named with GEM
     * conventions.
     * 
     * @return launcher for the synchronize environment. Can only be
     * launched if already cloned ({@link #getCloneLauncher} cannot launch).
     * 
     * @throws WorkflowException if there is a problem on the workflow.
     */
    public ILauncher getSynchronizeAllEnvBranchesLauncher() throws WorkflowException;

    /**
     * Setup the environment work-tree.
     * 
     * @return launcher for the setup worktree.
    
     */
    public ILauncher getSetupWorktreeLauncher() throws WorkflowException;

    /**
     * Removes the environment worktree.
     * </br>
     * WARNING: this deletes the filesystem work-tree and associated worktree
     * definition(s).
     * 
     * @return launcher for the synchronize environment.
     */
    public ILauncher getRemoveWorktreeLauncher() throws WorkflowException;

    /**
     * Gets the feature-branch workflow.
     * 
     * @param featureBranch feature-branch.
     * 
     * @return the workflow for the feature-branch
     */
    public IFeatureBranchWorkflow getFeatureBranchWorkflow(final IFeatureBranch featureBranch)
            throws WorkflowException;

}
