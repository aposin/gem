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
package org.aposin.gem.core.impl.internal.model.repo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.aposin.gem.core.Activator;
import org.aposin.gem.core.api.model.ILocalRepositoryDef;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.RepositoryException;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor.InstallScope;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.core.api.workflow.IRepositoryCommandBuilder;
import org.aposin.gem.core.api.workflow.exception.MergeConflictException;
import org.aposin.gem.core.impl.internal.util.CProcessExecutor;
import org.aposin.gem.core.impl.internal.util.GitConstants;
import org.aposin.gem.core.impl.internal.workflow.command.CallableCommand;
import org.aposin.gem.core.impl.internal.workflow.command.CallableCommand.CallableResult;
import org.aposin.gem.core.impl.internal.workflow.command.NoOpCommand;
import org.aposin.gem.core.impl.internal.workflow.command.ProcessCommand;
import org.aposin.gem.core.impl.internal.workflow.command.ResultBuilder;
import org.aposin.gem.core.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * Abstract implementation of git repository and workflow.
 */
public abstract class AbstractGitRepository
        implements ILocalRepositoryDef, IRepositoryCommandBuilder {

    private static final String WORKTREE_COMMAND = "worktree";
    private static final String FETCH_COMMAND = "fetch";
    private static final String CHECKOUT_COMMAND = "checkout";
    private static final String MERGE_COMMAND = "merge";
    
    private static final String PRUNE_FLAG = "--prune";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String branch = null;

    /**
     * Checks if a git-command could be made
     * (e.g., if the repository is cloned or the worktree added).
     * 
     * @throws RepositoryException if it is not present.
     */
    protected abstract void checkRunRequirements() throws RepositoryException;

    /**
     * Hook to add the branch to the repository after a process is run.
     * 
     * @param branch branch to add.
     */
    protected abstract void addBranch(final String branch);

    /**
     * Hook to remove the branch to the repository after a process is run.
     * 
     * @param branch branch to add.
     */
    protected abstract void removeBranch(final String branch);

    /**
     * Hook to add a worktree to the repository after a process is run.
     * 
     * @param location location of the worktree.
     * @param targetBranch target branch of the worktree.
     */
    protected abstract void addWorktree(final Path location, final String targetBranch);

    /**
     * Hook to remove a worktree from the repository after a process is run.
     * 
     * @param location location of the worktree.
     */
    protected abstract void removeWorktree(final Path location);

    /**
     * Hook to refresh the branches.
     */
    protected abstract void refreshBranches();

    /**
     * Gets the repository-scope.
     * </br>
     * In the case of a worktree, this is te repository where
     * the objects are stored.
     * 
     * @return
     */
    protected abstract IRepository getRepository();
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isClean() throws RepositoryException {
        checkRunRequirements();
        return haveNoDiffs() && haveNoUntracked();
    }

    private final boolean haveNoDiffs() {
        // NOTE: this cannot be cached as setting this info requires listening
        // for changes on the repository/worktree
        // returns 0/1 if it is clean or not
        final ProcessResult result =
                runGitCommand(false, new int[] {0, 1}, "diff-index", "--quiet", "HEAD");
        if (result == null) {
            throw new RepositoryException("Failed to run git diff-index");
        }
        switch (result.getExitValue()) {
            case 0:
                return true;
            case 1:
                return false;
            default:
                throw new RepositoryException(
                        "Unexpected exit code for diff-index: " + result.getExitValue());
        }
    }
    
    private final boolean haveNoUntracked() {
        final ProcessResult result = runGitCommand(true, "ls-files", "--other", "--exclude-standard");
        if (result == null) {
            throw new RepositoryException("Failed to run git ls-files");
        }
        return result.getOutput().getString().isEmpty();
    }
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getBranch() throws RepositoryException {
        if (branch == null) {
            checkRunRequirements();
            final ProcessResult result = runGitCommand(true, "branch", "--show-current");
            if (result != null) {
                branch = result.getOutput().getString().trim();
            }
        }
        return branch;
    }

    @Override
    public boolean containRemoteBranch(final String branch) {
        return getBranches().contains(GitConstants.ORIGIN + "/" + branch);
    }
    
    @Override
    public boolean containMatchingBranch(String branch) {
        final String originBranch = GitConstants.ORIGIN + "/" + branch;
        for (final String repoBranch : getBranches()) {
            if (repoBranch.equals(branch) || repoBranch.equals(originBranch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the branch where this repository is located.
     * </br>
     * Could be used to sub-classes to force a branch
     * even if the repository is not yet in the filesystem
     * or invalidate the cache setting to {@code null}.
     * 
     * @param branch branch to set.
     */
    protected final void setBranch(final String branch) {
        this.branch = branch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IRepositoryCommandBuilder getCommandBuilder() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void refresh() {
        branch = null;
        refreshBranches();
    }

    ///////////////////////////////////////
    // IRepositoryCommandBuilder methods

    /**
     * {@inheritDoc}
     */
    @Override
    public final ICommand buildCloneCommand() {
        final ProcessExecutor executor = newDefaultProcessExecutor()//
                .command(// e.g., "git clone git@github.com:allianz/gem.git C:/dev/gem/repos"
                        gitBinaryString(), "clone", //
                        getRepository().getUrl(), // <remote>
                        getRepository().getDestinationLocation().toString()); // <location>
        // add listener to refresh after cloning
        executor.addListener(new ProcessListener() {
            @Override
            public void afterFinish(final Process process, final ProcessResult result) {
                refresh();
            }
        });

        // the command-scope is the repository
        return new ProcessCommand(getRepository(), executor);
    }

    @Override
    public ICommand buildInstallHooksCommand(List<GemRepoHookDescriptor> hooks) {
        // the repo is the scope
        final IRepository repo = this.getRepository();

        if (hooks.isEmpty()) {
            return new NoOpCommand(repo);
        }

        return new CallableCommand(repo, "Installing hooks", new CallableResult() {

            @Override
            public IResult call() throws Exception {
                if (!repo.isCloned()) {
                    return getCommand()
                            .getFailedResult("Cannot install hooks into not cloned repository");
                }
                try {
                    final Path gitHookDir = repo.getDestinationLocation()
                            .resolve(GitConstants.GITDIR_FOLDER).resolve("hooks");
                    // delete the directory if it exists
                    if (Files.exists(gitHookDir)) {
                        IOUtils.deleteRecursivelyIgnoringErrors(gitHookDir);
                    }
                    // create the clean directory
                    Files.createDirectories(gitHookDir);
                    for (final InstallScope scope : InstallScope.values()) {
                        getStdOut().println("Initializing hook-scope: " + scope);
                        Files.createDirectory(gitHookDir.resolve(scope.getScriptDirectoryName()));
                        IOUtils.writeContent(Activator.getResource("scripts/githook.root.sh"),
                                gitHookDir.resolve(scope.getScriptName()));
                    }

                    for (final GemRepoHookDescriptor hookDesc : repo.getHooks()) {
                        final URI scriptLoc = hookDesc.getScriptLocation();
                        final String hookName = new File(scriptLoc.getPath()).getName();
                        getStdOut().println("Installing git-hook: " + hookName);
                        final InstallScope[] scopes = hookDesc.getInstallScope();
                        logger.debug("Installing for {} scopes: {}", scopes, scriptLoc);
                        for (final InstallScope scope : scopes) {
                            final Path hookLoc = gitHookDir//
                                    .resolve(scope.getScriptDirectoryName())//
                                    .resolve(hookName);
                            IOUtils.writeContent(scriptLoc, hookLoc);
                        }
                    }
                } catch (final IOException e) {
                    throw new RepositoryException("Cannot install hooks", e);
                }

                // return a successful result
                return new ResultBuilder(getCommand()).build();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ICommand buildAddWorktreeCommand(final Path location, final String targetBranch,
            final String baseBranch) {
        // IMPORTANT: should be on the repo location, not the worktree one
        final ProcessExecutor executor =
                newProcessExecutorOn(getRepository().getDestinationLocation());
        if (containMatchingBranch(targetBranch)) {
            executor.command(gitBinaryString(), WORKTREE_COMMAND, "add", //
                    location.toString(), // destination location
                    targetBranch); // targetBranch
        } else {
            // create the worktree:
            // git worktree -b <branch_name> <worktree_location> <base_branch>
            executor.command(gitBinaryString(), WORKTREE_COMMAND, "add", //
                    "-b", targetBranch, // create a new branch
                    location.toString(), // destination location
                    baseBranch); // baseBranch
            executor.addListener(new ProcessListener() {
                @Override
                public void afterFinish(Process process, ProcessResult result) {
                    addBranch(getBranch());
                }
            });
        }

        executor.addListener(new ProcessListener() {
            @Override
            public void afterFinish(Process process, ProcessResult result) {
                addWorktree(location, targetBranch);
            }
        });

        // the command-scope is the repository
        return new ProcessCommand(getRepository(), executor);
    }

    @Override
    public final ICommand buildRemoveWorktreeCommand(final Path location) {
        // always run on the repository location
        final ProcessExecutor executor =
                newProcessExecutorOn(getRepository().getDestinationLocation());
        executor.command(gitBinaryString(), WORKTREE_COMMAND, "remove", "--force",
                IOUtils.pathToString(location)); // convert to a proper string
        // add the hook to remove the worktree from the list of added worktrees
        executor.addListener(new ProcessListener() {
            @Override
            public void afterFinish(final Process process, final ProcessResult result) {
                removeWorktree(location);
            }
        });

        return new ProcessCommand(getRepository(), executor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ICommand buildCheckoutCommand(final String targetBranch, final String baseBranch) {
        final ProcessExecutor checkoutCommand = newProcessExecutorOn(getDestinationLocation());
        // when it finishes successfully, set the branch to the one that is checkout
        checkoutCommand.addListener(new ProcessListener() {
            @Override
            public void afterFinish(Process process, ProcessResult result) {
                // sets the branch and adds it
                setBranch(targetBranch);
                addBranch(targetBranch);
            }
        });

        // branch is already in the repo, does not requires to create and track
        if (containMatchingBranch(targetBranch)) {
            // git checkout <target>
            checkoutCommand.command(gitBinaryString(), CHECKOUT_COMMAND, targetBranch);
        } else {
            // git checkout -b <target> <start_point>
            checkoutCommand.command(gitBinaryString(), CHECKOUT_COMMAND, //
                    "-b", targetBranch, baseBranch);
        }
        // command-scope is the repo/worktree where this is called
        return new ProcessCommand(this, checkoutCommand);
    }

    @Override
    public final ICommand buildPullCommand() {
        // always use fast-forward (--ff) for merging if possible
        // and also default message (--no-edit) to avoid promt
        return withMergeConflictsException(
                new ProcessCommand(this, newProcessExecutorOn(getDestinationLocation())//
                        .command(gitBinaryString(), "pull", PRUNE_FLAG, "--no-edit", "--ff")));
    }

    @Override
    public final ICommand buildMergeCommand(final String branch) {
        // always use fast-forward (--ff) for merging if possible
        // and also default message (--no-edit) to avoid promt
        return withMergeConflictsException(
                new ProcessCommand(this, newProcessExecutorOn(getDestinationLocation())//
                        .command(gitBinaryString(), MERGE_COMMAND, "--no-edit", "--ff", branch)));
    }

    @Override
    public ICommand buildContinueMergeCommand() {
        // merging does not require a listener as does not change cache information
        // command-scope is the repo/worktree where this is called

        // the "-c core.editor=true" configuration is important to avoid
        // editor prompting for message (same as "--no-edit" for "merge --continue"
        return withMergeConflictsException(new ProcessCommand(this,
                newProcessExecutorOn(getDestinationLocation()) //
                        .command(gitBinaryString(), "-c", "core.editor=true", MERGE_COMMAND,
                                "--continue")));

    }

    private ProcessCommand withMergeConflictsException(final ProcessCommand cmd) {
        // transform excetpion to throw the merge-conflict one
        cmd.addExceptionTransformer(throwable -> {
            if (throwable instanceof InvalidExitValueException && !this.isClean()) {
                return new MergeConflictException("Merge conflicts", throwable);
            }
            return throwable;
        });
        return cmd;
    }

    @Override
    public ICommand buildAbortMergeCommand() {
        // this is only in case of failure
        return new ProcessCommand(this, newProcessExecutorOn(getDestinationLocation())
                .command(gitBinaryString(), MERGE_COMMAND, "--abort"));
    }

    @Override
    public final ICommand buildFetchCommand(final String targetBranch) {
        final ProcessExecutor executor = newProcessExecutorOn(getDestinationLocation());
        if (Objects.equals(targetBranch, getBranch())) {
            executor.command(gitBinaryString(), FETCH_COMMAND, PRUNE_FLAG);
        } else {
            // assumes same branch name -> e.g. cmd: "git fetch origin master:master"
            executor.command(gitBinaryString(), FETCH_COMMAND, //
                    GitConstants.ORIGIN, PRUNE_FLAG, //
                    targetBranch + ":" + targetBranch);

        }

        executor.addListener(new ProcessListener() {
            @Override
            public void afterFinish(Process process, ProcessResult result) {
                addBranch(targetBranch);
            }
        });

        // command-scope is the repo/worktree where this is called
        return new ProcessCommand(this, executor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommand buildFetchCommandPattern(final String branchPattern) {
        final ProcessExecutor executor = newProcessExecutorOn(getDestinationLocation());
        executor.command(gitBinaryString(), FETCH_COMMAND, //
                GitConstants.ORIGIN, PRUNE_FLAG, // prune to keep a clean and sane number of branches
                // + symbol indicates update the refs and do not fail on non-fast-forwar
                "+refs/heads/" + branchPattern + ":" + "refs/remotes/origin/" + branchPattern);


        executor.addListener(new ProcessListener() {
            @Override
            public void afterFinish(Process process, ProcessResult result) {
                // on finish, it should refresh all branches
                refreshBranches();
            }
        });

        return new ProcessCommand(this, executor);
    }

    /**
     * Builds a process executor to pull the current status of the work-tree.
     * 
     * @param config configuration.
     * @param localRepo definition of repo where push should be performed
     * @return process to execute.
     */
    @Override
    public final ICommand buildPushCommand() {
        final ProcessExecutor executor;
        final String currentBranch = getBranch();
        if (getBranches().contains(GitConstants.ORIGIN + "/" + currentBranch)) {
            executor = newProcessExecutorOn(getDestinationLocation()).command(gitBinaryString(),
                    "push");
        } else {
            executor = getPushSettingUpstreamExecutor(currentBranch);
        }
        return new ProcessCommand(this, executor);
    }

    private ProcessExecutor getPushSettingUpstreamExecutor(final String branch) {
        final ProcessExecutor executor = newProcessExecutorOn(getDestinationLocation());
        executor.command(gitBinaryString(), "push", "--set-upstream", GitConstants.ORIGIN, branch);
        executor.addListener(new ProcessListener() {
            @Override
            public void afterFinish(Process process, ProcessResult result) {
                // adds the origin too
                addBranch(GitConstants.ORIGIN + "/" + branch);
            }
        });
        return executor;
    }

    @Override
    public ICommand buildRemoveBranchCommand(final String branchName) {
        final ProcessExecutor executor = newProcessExecutorOn(getDestinationLocation());
        executor.exitValueAny();
        executor.command(gitBinaryString(), "branch", "-D", branchName);
        executor.addListener(new ProcessListener() {

            @Override
            public void afterFinish(Process process, ProcessResult result) {
                removeBranch(branchName);
                // remove also from origin to avoid showing again
                // on refresh it will appear anyway
                removeBranch(GitConstants.ORIGIN + "/" + branchName);
            }
        });
        return new ProcessCommand(this, executor);
    }

    @Override
    public ICommand buildCleanCommand() {
        return buildCleanTracked().and(buildCleanUntracked());
    }
    
    private ICommand buildCleanTracked() {
        final ProcessExecutor executor = newProcessExecutorOn(getDestinationLocation());
        executor.exitValueAny();
        // use checkout instead of restore, as restore is experimental
        executor.command(gitBinaryString(), CHECKOUT_COMMAND, "--", ".");
        return new ProcessCommand(this, executor);
    }
    
    private ICommand buildCleanUntracked() {
        final ProcessExecutor executor = newProcessExecutorOn(getDestinationLocation());
        executor.exitValueAny();
        // git clean --force -d to delete untracked files (recursively with -d)
        // not using -x, as ignored files shouldn't be removed
        executor.command(gitBinaryString(), "clean", "--force", "-d");
        return new ProcessCommand(this, executor);
    }
    
    //////////////////////////
    // HELPER METHODS TO RUN GIT COMMANDS

    private final String gitBinaryString() {
        return getConfiguration().getPreferences().getGitBinary().toString();
    }

    /**
     * Utility method to run a git command, reading or not the output.
     * 
     * @param readOutput
     * @param args
     * @return
     */
    protected final ProcessResult runGitCommand(final boolean readOutput, final String... args) {
        return runGitCommand(null, readOutput, new int[] {0}, args);
    }

    /**
     * Utility method to run a git command, reading or not the output.
     * 
     * @param readOutput
     * @param args
     * @return
     */
    protected final ProcessResult runGitCommand(final boolean readOutput, final int[] exitValues,
            final String... args) {
        return runGitCommand(null, readOutput, exitValues, args);
    }

    /**
     * Utility method to run a git command, reading or not the output.
     * 
     * @param readOutput
     * @param args
     * @return
     */
    private final ProcessResult runGitCommand(final ProcessListener listener,
            final boolean readOutput, final int[] exitValues, final String... args) {
        final List<String> cmd = new ArrayList<>(args.length);
        cmd.add(gitBinaryString());
        cmd.addAll(Arrays.asList(args));
        final ProcessExecutor exec = newProcessExecutorOn(getDestinationLocation()) //
                .readOutput(readOutput) //
                .exitValues(exitValues) //
                .command(cmd);
        if (listener != null) {
            exec.addListener(listener);
        }
        try {
            logger.debug("Running '{}' on [{}]", exec.getCommand(), exec.getDirectory());
            return exec.execute();
        } catch (final InvalidExitValueException | IOException | InterruptedException
                | TimeoutException e) {
            // if error, does not return anything
            logger.warn("Error running '{}'", exec.getCommand(), e);
            return null;
        }
    }

    /**
     * Utility method to create a {@link ProcessExecutor} to execute the git
     * commands.
     * </br>
     * This method should return a {@link CProcessExecutor} instance to be able
     * to work with the {@link ICommand} implementations.
     * 
     * @return new instance of the process executor already configured.
     */
    private ProcessExecutor newDefaultProcessExecutor() {
        // should use the CProcessExecutor
        return new CProcessExecutor() //
                .exitValueNormal() //
                .destroyOnExit() // always destroy any started process on exit (shutdown hook)
                .redirectOutput(Slf4jStream.of(logger).asTrace())
                .redirectError(Slf4jStream.of(logger).asError());
    }

    private ProcessExecutor newProcessExecutorOn(final Path path) {
        return newDefaultProcessExecutor().directory(path.toFile());
    }

}
