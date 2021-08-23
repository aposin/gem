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

import java.nio.file.Path;
import java.util.List;
import org.aposin.gem.core.api.model.ILocalRepositoryDef;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.core.api.workflow.exception.MergeConflictException;

/**
 * Gets the workflow for the repository.
 */
public interface IRepositoryCommandBuilder {

    /**
     * Builds the command to clone the the repository.
     * </br>
     * IMPORTANT: This is supposed to be done only once.
     * 
     * @return command.
     */
    public ICommand buildCloneCommand();

    /**
     * Do a clean installation of the hooks into the repository.
     * </br>
     * This command removes all other hooks in the repository.
     * TODO: should it have a parameter to control this?
     * 
     * @param hooks list of provided hooks to be installed.
     * 
     * @return command.
     */
    public ICommand buildInstallHooksCommand(final List<GemRepoHookDescriptor> hooks);

    /**
     * Builds the command to add a worktree based on the repository's branch.
     * 
     * @return
     */
    public ICommand buildAddWorktreeCommand(final Path location, final String targetBranch,
            final String baseBranch);

    /**
     * Builds the command to remove a worktree from the location.
     * 
     * @param location location to remove the worktreee.
     * @return command
     */
    public ICommand buildRemoveWorktreeCommand(final Path location);

    /**
     * Builds the command to checkout a repository's branch.
     * </br>
     * If the branch does not exists, get it from the base branch.
     * 
     * @param branch
     * @param baseBranch branch
     * @return command
     */
    // TODO - change for git-switch -> only for git version > 2.23.0
    // TODO - e.g. , git switch -c <my_branch> <start_point>
    public ICommand buildCheckoutCommand(final String branch, final String baseBranch);

    /**
     * Builds the command to fetch the repository's branch.
     * 
     * @return command.
     */
    public ICommand buildFetchCommand(final String branch);

    /**
     * Builds the command to fetch several branches at once using a pattern.
     * </br>
     * The pattern for git should not contain the refs definitions
     * (e.g., refs/heads or refs/remotes), just the short-form.
     * 
     * @param branchPattern pattern to match the branches.
     * @return command.
     */
    public ICommand buildFetchCommandPattern(final String branchPattern);

    /**
     * Builds the command to pull the repository's current branch.
     * </br>
     * If the command is run and fails due to conflicts, {@link IResult#getException()}
     * should return a {@link MergeConflictException}.
     * 
     * @return command.
     */
    public ICommand buildPullCommand();

    /**
     * Builds the command to merge a branch into the repository's current branch.
     * </br>
     * If the command is run and fails due to conflicts, {@link IResult#getException()}
     * should return a {@link MergeConflictException}.
     * 
     * @param branch
     * @return command.
     */
    public ICommand buildMergeCommand(final String branch);

    /**
     * Builds a command to continue an on-hold merge.
     * </br>
     * If the command is run and fails due to conflicts, {@link IResult#getException()}
     * should return a {@link MergeConflictException}.
     * 
     * @return command
     */
    public ICommand buildContinueMergeCommand();

    /**
     * Builds a command to abort an on-hold merge.
     * 
     * @return
     */
    public ICommand buildAbortMergeCommand();

    /**
     * Builds the command to push the current branch into the remote repository.
     * 
     * @return
     */
    public ICommand buildPushCommand();

    /**
     * Build the command to remove the provided branch.
     * 
     * @param branchName branch to remove.
     * @return
     */
    public ICommand buildRemoveBranchCommand(final String branchName);
    // TODO - there is a command to also delete the remote branch
    // TODO - by doing "git push origin --delete ${branch_name}"
    // TODO - but not sure if it corresponds here or a different
    // TODO - method (e.g., buildRemoveRemoteBranchCommand)

    /**
     * Build the command to clean the repository.
     * </br>
     * This command should perform any action to revert the status of the
     * repository in a way that {@link ILocalRepositoryDef#isClean()}
     * returns {@code true}.
     * 
     * @return command to clean the repository.
     */
    public ICommand buildCleanCommand();
    
}
