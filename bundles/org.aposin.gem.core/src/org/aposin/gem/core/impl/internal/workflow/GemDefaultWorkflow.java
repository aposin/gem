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
package org.aposin.gem.core.impl.internal.workflow;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.api.workflow.IFeatureBranchWorkflow;
import org.aposin.gem.core.api.workflow.WorkflowException;
import org.aposin.gem.core.api.workflow.exception.MergeConflictException;
import org.aposin.gem.core.impl.internal.workflow.command.CallableCommand;
import org.aposin.gem.core.impl.internal.workflow.command.CallableCommand.CallableResult;
import org.aposin.gem.core.impl.internal.workflow.command.FailCommand;
import org.aposin.gem.core.impl.internal.workflow.command.NoOpCommand;
import org.aposin.gem.core.impl.internal.workflow.command.ResultBuilder;
import org.aposin.gem.core.impl.internal.workflow.command.RetryCommand;

/**
 * Default workflow for git repositories.
 */
public final class GemDefaultWorkflow extends AbstractGemWorkflow {

    // TODO - move to some message properties?
    private static final String ENV_WORKTREE_REQUIRED = "Environment worktree is required";
    private static final String FB_CHECKOUT_REQUIRED = "Feature-branch checkout is required";
    private static final String FB_CHECKOUT_NOT_REQUIRED =
            "Feature-branch checkout is not required";

    public GemDefaultWorkflow(final IEnvironment environment) {
        super(environment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresClone() {
        for (final IRepository repo : getEnvironment().getRepositories()) {
            if (!repo.isCloned()) {
                // if any of the repos is not cloned, requires to clone
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresWorktreeSetup() throws WorkflowException {
        if (!requiresClone()) {
            for (final IWorktreeDefinition worktree : getEnvironment().getEnvironmentWorktrees()) {
                if (!worktree.isAdded()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean removeWorktreeIsEnabled() {
        if (!requiresClone()) {
            for (final IWorktreeDefinition worktree : getEnvironment().getEnvironmentWorktrees()) {
                if (worktree.isAdded() || Files.exists(worktree.getDestinationLocation())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public IFeatureBranchWorkflow getFeatureBranchWorkflow(final IFeatureBranch featureBranch)
            throws WorkflowException {
        if (featureBranch == null || !getEnvironment().equals(featureBranch.getEnvironment())) {
            throw new IllegalArgumentException("Invalid call for feature-branch workflow for '"
                    + featureBranch + "'; environment does not match");
        }
        // check workflow previous steps
        if (requiresWorktreeSetup()) {
            throw new WorkflowException(ENV_WORKTREE_REQUIRED);
        }

        return new FeatureBranchWorkflow(featureBranch);
    }

    /**
     * Internal implementation of FeatureBranchWorkflow
     */
    private class FeatureBranchWorkflow implements IFeatureBranchWorkflow {

        private final IFeatureBranch featureBranch;

        public FeatureBranchWorkflow(final IFeatureBranch featureBranch) {
            this.featureBranch = featureBranch;
        }

        @Override
        public IFeatureBranch getFeatureBranch() {
            return featureBranch;
        }

        private boolean requiresCheckout() throws WorkflowException {
            for (final IWorktreeDefinition worktreeDef : featureBranch.getEnvironment()
                    .getEnvironmentWorktrees()) {
                if (!worktreeDef.isAdded()) {
                    throw new WorkflowException(ENV_WORKTREE_REQUIRED);
                }
                if (requiresCheckoutFeatureBranch(worktreeDef)) {
                    return true;
                }
            }
            return false;
        }

        private boolean requiresCheckoutFeatureBranch(final IWorktreeDefinition worktreeDef) {
            final String checkoutBranch =
                    featureBranch.getCheckoutBranch(worktreeDef.getRepository());
            if (!worktreeDef.getBranch().equals(checkoutBranch)) {
                return true;
            }
            return false;
        }

        @Override
        public ILauncher getFetchAndCheckoutLauncher() throws WorkflowException {
            return new WorkflowLauncherBuilder(featureBranch, "fetch_checkout_branch") //
                    .displayName("Fetch & Checkout " + featureBranch.getDisplayName()) //
                    .exceptionMessage(FB_CHECKOUT_NOT_REQUIRED) //
                    .canLaunch(() -> !requiresClone() && requiresCheckout()) //
                    .build(() ->
                    {
                    // first check if all worktrees are clean
                    ICommand command = getCheckCleanWorktreesCommand("Checkout (all repositories)");
                        final String internalGemBranch = getEnvironment().getGemInternalBranchName();
                        for (final IWorktreeDefinition worktree : getEnvironment().getEnvironmentWorktrees()) {
                        if (requiresCheckoutFeatureBranch(worktree)) {
                            final String checkoutBranch =
                                    featureBranch.getCheckoutBranch(worktree.getRepository());
                            final String baseBranch =
                                        getEnvironment().getEnvironmentBranch(worktree.getRepository());
                            final boolean isInternalBranch = internalGemBranch.equals(checkoutBranch);
                            // first fetch the first branch (if not the internal branch)
                            if (!isInternalBranch) {
                                    command = command.and(getFetchOrPullRepoCommand(worktree.getRepository()));
                            }
                            command = command.and(worktree.getCommandBuilder()
                                    .buildCheckoutCommand(checkoutBranch, baseBranch));
                            // after checkout, if it is an internal branch, update automatically
                            if (isInternalBranch) {
                                    command = command.and(worktree.getCommandBuilder().buildMergeCommand(baseBranch));
                            }
                        }
                    }

                    return Collections.singletonList(command);
                    });
        }

        private ICommand getCheckCleanWorktreesCommand(final String description) {
            return new CallableCommand(getEnvironment(), description, new CallableResult() {

                @Override
                public IResult call() throws Exception {
                    getStdOut().println("Checking repository status");
                    final List<String> dirtyWortreeString = getEnvironment() //
                            .getEnvironmentWorktrees().stream() //
                            .filter(w -> !w.isClean()) //
                            .map(w -> w.getName()) //
                            .collect(Collectors.toList());
                    if (!dirtyWortreeString.isEmpty()) {
                        getStdErr().println("Environment should be clean to checkout.");
                        getStdErr().println("Dirty worktrees:");
                        for (final String dirty: dirtyWortreeString) {
                            getStdErr().println("  * " + dirty);
                        }
                        return getCommand().getFailedResult("Dirty environment worktrees: " + dirtyWortreeString);
                    }
                    return new ResultBuilder(getCommand()).build();
                }
            });
        }

        @Override
        public ILauncher getMergeBaseIntoFeatureBranchLauncher(final Function<IWorktreeDefinition, Boolean> shouldAbort)
                throws WorkflowException {
            // TODO - maybe allow params for this
            // TODO - for example, the "--no-commit" might be useful to stop before commiting
            return new WorkflowLauncherBuilder(featureBranch, "merge_branch") //
                    .displayName("Fetch & Merge " + featureBranch.getDisplayName()) //
                    .exceptionMessage(FB_CHECKOUT_REQUIRED) //
                    .canLaunch(() -> !(requiresClone() || requiresCheckout())) //
                    .build(() ->
                    {
                        final Map<IRepository, String> branchByRepo = getEnvironment()
                                .getEnvironmentBranchByRepository();

                        // start with check/fetch & merge 
                        ICommand command = getCheckCleanWorktreesCommand("Merging from base-branch (all repositories)");
                        // accumulator for merge commands to add at the end
                        final List<ICommand> mergeCommands = new ArrayList<>();
                        for (final IWorktreeDefinition worktree : getEnvironment().getEnvironmentWorktrees()) {
                            // AND fetch in all repos before merge
                            command = command.and(//
                                    getFetchOrPullRepoCommand(worktree.getRepository()));
                            final String baseBranch = branchByRepo.get(worktree.getRepository());
                            // composed merge command with conflict retry
                            final ICommand merge = composeWithMergeConflictRetryOrAbort(worktree,
                                    worktree.getCommandBuilder().buildMergeCommand(baseBranch), shouldAbort);
                            mergeCommands.add(merge);
                        }
                        // add the merge command after the check & fetch
                        for (final ICommand mergeCmd : mergeCommands) {
                            command = command.and(mergeCmd);
                        }
                        return Collections.singletonList(command);
                    });
        }

        @Override
        public ILauncher getRemoveBranchLauncher() throws WorkflowException {
            return new WorkflowLauncherBuilder(featureBranch, "remove_branch") //
                    .displayName("Remove " + featureBranch.getDisplayName()) //
                    .canLaunch(() -> {
                        if (requiresClone()) {
                            // if it is not cloned we cannot delete anything repository-related
                            // if it is checkout already, cannot delete as it should change to a different branch
                            return false;
                        }
                        boolean anyMatch = false;
                        for (final IWorktreeDefinition wt : featureBranch.getEnvironment()
                                .getEnvironmentWorktrees()) {
                            final String checkoutBranch =
                                    featureBranch.getCheckoutBranch(wt.getRepository());
                            if (checkoutBranch.startsWith(IEnvironment.INTERNAL_BRANCH_PREFIX)) {
                                return false;
                            }
                            if (wt.containMatchingBranch(checkoutBranch)) {
                                anyMatch = true;
                            }

                        }
                        return anyMatch;
                    }) //
                    .build(() -> getCommandListByWorktree(w -> true, // all worktrees
                            this::createRemoveBranchCommand));
        }

        // creates a removeFeatureBranch command for the worktree
        private ICommand createRemoveBranchCommand(final IWorktreeDefinition worktree) {
            final IRepository repo = worktree.getRepository();
            final String checkoutBranch = featureBranch.getCheckoutBranch(repo);
            // remove command always
            ICommand command = worktree.getCommandBuilder().buildRemoveBranchCommand(checkoutBranch);
            if (worktree.getBranch().equals(checkoutBranch)) {
                final String baseBranch = featureBranch.getEnvironment().getEnvironmentBranch(repo);
                final String internalBranchName = featureBranch.getEnvironment().getGemInternalBranchName();
                final ICommand checkoutInternalBranch = worktree.getCommandBuilder()
                        .buildCheckoutCommand(internalBranchName, baseBranch);
                // befroe it should chekcout the internal branch in this case
                command = checkoutInternalBranch.and(command);
            }
            return command;
        }

        private ICommand composeWithMergeConflictRetryOrAbort(final IWorktreeDefinition worktree,
                                                              final ICommand mergeCommand,
                                                              final Function<IWorktreeDefinition, Boolean> shouldAbort) {
            final ICommand continueMerge = worktree.getCommandBuilder().buildContinueMergeCommand();
            final ICommand abortMerge = worktree.getCommandBuilder().buildAbortMergeCommand();

            return mergeCommand.or(//
                    new RetryCommand(continueMerge, result -> {
                        if (result.getException() instanceof MergeConflictException) {
                            return shouldAbort.apply(worktree);
                        }
                        return true;
                    }).or(abortMerge).and(new FailCommand(worktree, "ABORTED MERGE")));

        }

        @Override
        public ILauncher getPullLauncher(final Function<IWorktreeDefinition, Boolean> shouldAbort)
                throws WorkflowException {
            return new WorkflowLauncherBuilder(featureBranch, "pull") //
                    .displayName("Pull " + featureBranch.getDisplayName()) //
                    .exceptionMessage(FB_CHECKOUT_REQUIRED) //
                    .canLaunch(() ->
                    {
                        // if requires clone or checkout, cannot launch
                        if (requiresClone() || requiresCheckout()) {
                            return false;
                        }
                        // otherwise, should check if any worktree requires checkout
                        return getEnvironment().getEnvironmentWorktrees().stream()//
                                .anyMatch(wt -> canMergeOrPullWorktree(wt));
                    }) //
                    .build(() -> {
                        // similar to merge
                        // first check if all worktrees are clean
                        final String internalBranch = getEnvironment().getGemInternalBranchName();
                        ICommand command = getCheckCleanWorktreesCommand(
                                "Pulling from remote-branch (all repositories)");
                        for (final IWorktreeDefinition worktree : getEnvironment().getEnvironmentWorktrees()) {
                            final String checkoutBranch = featureBranch.getCheckoutBranch(worktree.getRepository());

                            if (canMergeOrPullWorktree(worktree)) {
                                final ICommand pullCommand;
                                if (internalBranch.equals(checkoutBranch)) {
                                    final String envBranch = getEnvironment()
                                            .getEnvironmentBranch(worktree.getRepository());
                                    pullCommand = worktree.getCommandBuilder().buildMergeCommand(envBranch);
                                } else {
                                    pullCommand = worktree.getCommandBuilder().buildPullCommand();
                                }

                                command = command
                                        .and(composeWithMergeConflictRetryOrAbort(worktree, pullCommand, shouldAbort));
                            } else {
                                command = command.and(new NoOpCommand(worktree, //
                                        "Ignoring pull for " + worktree.getDisplayName()
                                                + ": push is required to track the branch remotely"));
                            }
                        }

                        return Collections.singletonList(command);
                    });
        }

        private boolean canMergeOrPullWorktree(final IWorktreeDefinition worktree) {
            final String internalBranch = getEnvironment().getGemInternalBranchName();
            final String checkoutBranch = featureBranch.getCheckoutBranch(worktree.getRepository());

            return internalBranch.equals(checkoutBranch) // internal branch (merge)
                    || worktree.containRemoteBranch(checkoutBranch); // remote branch (pull)
        }
        
        @Override
        public ILauncher getCleanWorktreeLauncher() throws WorkflowException {
            return new WorkflowLauncherBuilder(featureBranch, "clean_worktree") //
                    .displayName("Clean/Restore " + getEnvironment().getDisplayName()) //
                    .exceptionMessage(ENV_WORKTREE_REQUIRED) //
                    .canLaunch(() -> !(requiresClone() || requiresCheckout())) //
                    .build(() -> getCommandListByWorktree(wt -> true, // all worktrees
                            wt -> wt.getCommandBuilder().buildCleanCommand()));
        }

    }
}
