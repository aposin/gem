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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.core.api.workflow.IEnvironmentWorkflow;
import org.aposin.gem.core.api.workflow.WorkflowException;
import org.aposin.gem.core.impl.internal.workflow.command.CallableCommand;
import org.aposin.gem.core.impl.internal.workflow.command.CallableCommand.CallableResult;
import org.aposin.gem.core.impl.internal.workflow.command.NoOpCommand;
import org.aposin.gem.core.impl.internal.workflow.command.ResultBuilder;
import org.aposin.gem.core.utils.IOUtils;

abstract class AbstractGemWorkflow implements IEnvironmentWorkflow {

    // TODO - move to some message properties?
    private static final String CLONE_NOT_REQUIRED_MSG = "Clone is already done";
    private static final String ENV_WORKTREE_NOT_REQUIRED = "Environment worktree not required";

    private final IEnvironment environment;

    protected AbstractGemWorkflow(final IEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Checks if any repository for the environment requires a clone.
     * 
     * @return {@code true} if clone is required; {@code false} otherwise.
     */
    protected abstract boolean requiresClone();

    /**
     * Checks if the environment's work-tree requires setup.
     * 
     * @return {@code true} if the worktree requires setup; {@code false} otherwise
     */
    protected abstract boolean requiresWorktreeSetup();

    /**
     * Checks if the remove worktree is enabled.
     * 
     * @return {@code true} if worktree removal is enabled; {@code false} otherwise.
     */
    protected abstract boolean removeWorktreeIsEnabled();

    ///////////////////////////////////////
    // INTERFACE METHODS
    //////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public final IEnvironment getEnvironment() {
        return environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ILauncher getCloneLauncher() throws WorkflowException {
        return new WorkflowLauncherBuilder(environment, "clone_repos") //
                .displayName("Clone") //
                .exceptionMessage(CLONE_NOT_REQUIRED_MSG) //
                .canLaunch(this::requiresClone) //
                .build(() -> getCommandListByRepo(//
                        (r, b) -> !r.isCloned(), // only not cloned
                        (r, b) -> r.getCommandBuilder().buildCloneCommand()
                                // after clone, install the hooks
                                .and(r.getCommandBuilder().buildInstallHooksCommand(r.getHooks()))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ILauncher getSynchronizeAllEnvBranchesLauncher() throws WorkflowException {
        return new WorkflowLauncherBuilder(environment, "pull_fetch_env") //
                .displayName("Pull/Fetch (Env)") //
                .exceptionMessage(CLONE_NOT_REQUIRED_MSG) //
                .canLaunch(() -> !this.requiresClone()) //
                .build(() ->
                {
                    final List<ICommand> cmds = new ArrayList<>();
                    for (final IRepository repo : environment.getRepositories()) {
                        // first fetch or pull
                        final ICommand fetchOrPullEnv = getFetchOrPullRepoCommand(repo);

                        // then fetch branches matching GEM-formatted ones
                        final String envBranchPattern = environment.getBranchPrefix()
                                + IEnvironment.BRANCH_NAME_SEPARATOR + "*";
                        final ICommand envPatternFetch = repo.getCommandBuilder() //
                                .buildFetchCommandPattern(envBranchPattern);

                        // add to the command list
                        cmds.add(fetchOrPullEnv.and(envPatternFetch));
                    }

                    return cmds;
                });
    }

    @Override
    public final ILauncher getSetupWorktreeLauncher() throws WorkflowException {
        return new WorkflowLauncherBuilder(environment, "setup_worktree") //
                .displayName("Create Worktree") //
                .exceptionMessage(ENV_WORKTREE_NOT_REQUIRED) //
                .canLaunch(this::requiresWorktreeSetup) //
                .build(() ->
                {
                    final Map<IRepository, String> branchByRepo = environment.getEnvironmentBranchByRepository();

                    return getCommandListByWorktree(//
                            w -> !w.isAdded(), // only added ones
                            w -> buildSetupWorktreeCommand(w, branchByRepo));
                });
    }

    private ICommand buildSetupWorktreeCommand(final IWorktreeDefinition worktree,
                                               final Map<IRepository, String> branchByRepo) {
        final String branch = branchByRepo.get(worktree.getRepository());
        ICommand setupWorktreeCommand;
        // first fetch the environment
        setupWorktreeCommand = getFetchOrPullRepoCommand(worktree.getRepository());
        // then add the worktree
        setupWorktreeCommand = setupWorktreeCommand.and(//
                worktree.getCommandBuilder().buildAddWorktreeCommand(worktree.getDestinationLocation(),
                        worktree.getBranch(), branch));
        return setupWorktreeCommand;
    }

    @Override
    public final ILauncher getRemoveWorktreeLauncher() throws WorkflowException {
        return new WorkflowLauncherBuilder(environment, "remove_worktree") //
                .displayName("Remove Worktree") //
                .canLaunch(this::removeWorktreeIsEnabled) //
                .exceptionMessage("Worktree-removal is not enabled") //
                .build(() ->
                {
                    final List<IWorktreeDefinition> worktreeDefs = environment.getEnvironmentWorktrees();
                    // start with a no-op only for information
                    ICommand command = new NoOpCommand(environment, "Worktree(s) removal");
                    for (final IWorktreeDefinition worktree : worktreeDefs) {
                        // only if it is added
                        if (worktree.isAdded()) {
                            // remove worktree using git
                            command = command.and(worktree.getCommandBuilder().buildRemoveWorktreeCommand(//
                                    worktree.getDestinationLocation()));
                        }
                        // remove also the internal branch from the repository (otherwise appear as an obsolete environment)
                        command = command.and(worktree.getRepository().getCommandBuilder()//
                                .buildRemoveBranchCommand(environment.getGemInternalBranchName()));
                        // TODO: also remove environment-based branches that are found by GEM
                        // TODO: this requires probably to use a parameter-based launcher
                    }

                    command = command.and(new CallableCommand(environment, "Delete worktree(s) folder container", //
                            new CallableResult() {

                                @Override
                                public IResult call() throws Exception {
                                    if (Files.exists(environment.getWorktreesBaseLocation())) {
                                        getStdOut().println("Delete folder (might fail if locked)");
                                        IOUtils.deleteRecursivelyIgnoringErrors(environment.getWorktreesBaseLocation());
                                        if (Files.exists(environment.getWorktreesBaseLocation())) {
                                            getStdErr().println(
                                                    "Worktree deletion was not successful: folder might be locked by other applications!");
                                            return getCommand().getFailedResult(
                                                    "Worktree deletion was not successful: folder might be locked by other applications!");
                                        }
                                    } else {
                                        getStdOut().println("Nothing to delete");
                                    }
                                    // construct non-failing result
                                    return new ResultBuilder(getCommand()).build();
                                }
                            }));

                    return Collections.singletonList(command);
                });
    }

    /////////////////////////////////////
    // HELPER METHODS
    ////////////////////////////////////

    protected final ICommand getFetchOrPullRepoCommand(final IRepository r) {
        final String envBranch = environment.getEnvironmentBranch(r);
        return envBranch.equals(r.getBranch()) // for the same branch pull
                ? r.getCommandBuilder().buildPullCommand() // otherwise, fetch
                : r.getCommandBuilder().buildFetchCommand(envBranch);
    }

    /**
     * Helper method to get a command by repository.
     * 
     * @param repoFilter     filter to check if on the repository, the action should
     *                       be performed.
     * @param commandBuilder builds a command from the repository and the branch.
     * 
     * @return list of commands (empty if none is required).
     */
    private final List<ICommand> getCommandListByRepo( //
                                                      final BiPredicate<IRepository, String> repoFilter, //
                                                      final BiFunction<IRepository, String, ICommand> commandBuilder) {
        final Map<IRepository, String> branchByrepo = environment.getEnvironmentBranchByRepository();
        final List<ICommand> commands = new ArrayList<>(branchByrepo.size());
        for (final Map.Entry<IRepository, String> repoInfo : branchByrepo.entrySet()) {
            final IRepository repository = repoInfo.getKey();
            final String branch = repoInfo.getValue();
            if (repoFilter.test(repository, branch)) {
                commands.add(commandBuilder.apply(repository, branch));
            }
        }
        return commands;
    }

    protected final List<ICommand> getCommandListByWorktree( //
                                                          final Predicate<IWorktreeDefinition> worktreeFilter, //
                                                          final Function<IWorktreeDefinition, ICommand> commandBuilder) {
        final List<ICommand> cmds = new ArrayList<>();
        for (final IWorktreeDefinition worktree : environment.getEnvironmentWorktrees()) {
            if (worktreeFilter.test(worktree)) {
                cmds.add(commandBuilder.apply(worktree));
            }
        }

        return cmds;
    }

}
