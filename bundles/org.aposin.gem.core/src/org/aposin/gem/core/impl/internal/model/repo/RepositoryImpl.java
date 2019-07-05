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
package org.aposin.gem.core.impl.internal.model.repo;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.model.RepositoryException;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;
import org.aposin.gem.core.impl.internal.config.bean.GemCfgBean.RepositoryBean;
import org.aposin.gem.core.impl.internal.util.GitConstants;
import org.aposin.gem.core.impl.model.repo.CoreGemGitHook;
import org.zeroturnaround.exec.ProcessResult;

public class RepositoryImpl extends AbstractGitRepository implements IRepository {

    protected final IConfiguration config;
    private final RepositoryBean repoBean;

    // cached data that could be refreshed
    private Set<String> branches = null;
    private Set<WorktreeRepoDefImpl> worktrees = null;
    private Map<Path, WorktreeRepoDefImpl> worktreesByPath = new HashMap<>();

    public RepositoryImpl(final IConfiguration config, final RepositoryBean repoBean) {
        this.config = config;
        this.repoBean = repoBean;
        refresh();
    }

    @Override
    public IConfiguration getConfiguration() {
        return config;
    }

    @Override
    public String getId() {
        return repoBean.id;
    }

    @Override
    public String getUrl() {
        return repoBean.url;
    }

    @Override
    public URI getServer() {
        return URI.create(repoBean.server);
    }

    @Override
    public Path getDestinationLocation() {
        return config.getRepositoriesDirectory().resolve(getId());
    }

    @Override
    public boolean isCloned() {
        final Path gitDir = getDestinationLocation().resolve(GitConstants.GITDIR_FOLDER);
        return Files.exists(gitDir) && Files.isDirectory(gitDir);
    }

    @Override
    public List<GemRepoHookDescriptor> getHooks() {
        final List<GemRepoHookDescriptor> gitHooks = new ArrayList<>(CoreGemGitHook.getDefaults());
        repoBean.hooks.stream() //
                .map(hook -> new GemRepoHookDescriptor() {

                    @Override
                    public URI getScriptLocation() {
                        return config.getRelativeToConfigFile(hook.path).toUri();
                    }

                    @Override
                    public InstallScope[] getInstallScope() {
                        return hook.scopes.toArray(InstallScope[]::new);
                    }

                }) //
                .forEach(gitHooks::add);

        return gitHooks;
    }

    @Override
    public synchronized List<String> getBranches() throws RepositoryException {
        checkRunRequirements();
        if (branches == null) {
            // git for-each-ref --format='%(refname:short)'
            // only refs/heads and refs/remotes to avoid tags
            final ProcessResult output = runGitCommand(true, "for-each-ref",
                    "--format=%(refname:short)", "refs/heads", "refs/remotes");
            if (output == null || output.getExitValue() != 0) {
                logger.warn("Error running for-each-ref process. Not branches fetched by the repo");
                branches = new TreeSet<>();
            } else {
                branches = output.getOutput().getLines().stream()//
                        // filter out the origin/HEAD
                        .filter(s -> !s.endsWith(GitConstants.ORIGIN + "/HEAD")) //
                        .collect(Collectors.toCollection(TreeSet::new));
            }
        }
        return List.copyOf(branches);
    }

    @Override
    public List<IWorktreeDefinition> getWorktrees() throws RepositoryException {
        loadWorktrees();
        return List.copyOf(worktreesByPath.values());
    }

    private void loadWorktrees() throws RepositoryException {
        checkRunRequirements();
        if (worktrees == null) {
            worktrees = runWorktreeList();
            // TODO - what to do with deprecated worktrees?
            for (final WorktreeRepoDefImpl worktreeDef : worktrees) {
                worktreesByPath.put(worktreeDef.getDestinationLocation(), worktreeDef);
            }
        }
    }

    /**
     * Gets the worktrees that are considered added to the repository.
     * 
     * @return added worktrees set.
     * @throws RepositoryException if there is any error while loading.
     */
    /* package */ Set<WorktreeRepoDefImpl> getAddedWorktrees() throws RepositoryException {
        loadWorktrees();
        return worktrees;
    }

    @Override
    public IWorktreeDefinition getWorktree(final Path worktreePath,
            final Supplier<String> branchSupplier) {
        if (isCloned()) {
            // triggers the worktree loading if need it
            getWorktrees();
        }
        // put the worktree in the map
        return worktreesByPath.computeIfAbsent(worktreePath,
                path -> new WorktreeRepoDefImpl(this, path, branchSupplier.get()));
    }

    public final Set<WorktreeRepoDefImpl> runWorktreeList() {
        final Set<WorktreeRepoDefImpl> parsedWorktrees = new LinkedHashSet<>();
        final ProcessResult output = runGitCommand(true, "worktree", "list", "--porcelain");
        if (output != null && output.getExitValue() == 0) {
            final String[] worktreeDefs = output.getOutput().getString().split("\n\\s*\n");
            Arrays.stream(worktreeDefs)//
                    .map(def -> getWorktreeFromPorcelainOutput(this, def)) //
                    .filter(Objects::nonNull) //
                    .forEach(parsedWorktrees::add);
        }
        return parsedWorktrees;
    }

    private static final WorktreeRepoDefImpl getWorktreeFromPorcelainOutput(
            final RepositoryImpl repo, final String lines) {
        final StringTokenizer st = new StringTokenizer(lines, "\n\r");
        Path worktreePath = null;
        String branch = null;
        while (st.hasMoreElements() && (worktreePath == null || branch == null)) {
            final String nextElement = st.nextToken();
            if (nextElement.startsWith("worktree")) {
                worktreePath = Paths.get(nextElement.split(" ")[1]);
            } else if (nextElement.startsWith("branch")) {
                branch = nextElement.split(" ")[1];
            }
        }
        if (branch != null && worktreePath != null) {
            // replace the worktree branch prefix
            final String worktreeBranch = branch//
                    .replaceFirst("refs/heads/", "");
            return new WorktreeRepoDefImpl(repo, worktreePath, worktreeBranch);
        }
        return null;
    }

    @Override
    protected void refreshBranches() {
        logger.debug("Refreshing repository branches and worktrees");
        branches = null;
        // delete the worktree definitions on the repository, but keep the ones not added
        if (worktrees != null) {
            for (final IWorktreeDefinition worktreeDef : worktrees) {
                worktreesByPath.remove(worktreeDef.getDestinationLocation());
            }
            worktrees = null;
        }
    }

    @Override
    protected IRepository getRepository() {
        return this;
    }

    /**
     * Checks if the repository is cloned.
     */
    @Override
    protected void checkRunRequirements() throws RepositoryException {
        if (!isCloned()) {
            throw new RepositoryException("Cant't perform action when repository isn't cloned!");
        }
    }

    /**
     * Adds the branch to the list of branches
     */
    @Override
    protected void addBranch(final String branch) {
        if (branches == null) {
            // trigger branches loaded if they were refreshed
            getBranches();
        }
        this.branches.add(branch);
    }

    /**
     * Remove the branch from the list of branches.
     */
    @Override
    protected void removeBranch(final String branch) {
        if (branches == null) {
            // trigger branches loaded if they were refreshed
            getBranches();
        }
        this.branches.remove(branch);
    }

    /**
     * Adds the worktree to the set of worktres (as added).
     */
    @Override
    protected void addWorktree(final Path location, final String targetBranch) {
        // compute if not present and add it to the worktrees if initialized
        final WorktreeRepoDefImpl worktreeDefinition = worktreesByPath.computeIfAbsent(location,
                path -> new WorktreeRepoDefImpl(this, path, targetBranch));
        worktreeDefinition.setBranch(targetBranch);
        if (isCloned()) {
            loadWorktrees();
            worktrees.add(worktreeDefinition);
        }
    }

    /**
     * Adds the worktree to the set of worktres (as added).
     */
    @Override
    protected void removeWorktree(final Path location) {
        final WorktreeRepoDefImpl worktreeDefinition = worktreesByPath.remove(location);
        if (worktreeDefinition != null && worktrees != null) {
            worktrees.remove(worktreeDefinition);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof IRepository) {
            return compareTo((IRepository) obj) == 0;
        }

        return false;
    }

    @Override
    public final String toString() {
        return super.toString() + ":" + repoBean.toString();
    }
}
