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
package org.aposin.gem.core.impl.internal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.impl.internal.util.GitConstants;

public abstract class AbstractGitBranchProvider implements IFeatureBranchProvider, IConfigurable {

    private IConfiguration config;
    private Map<IRepository, Set<String>> blackListedBranches;

    @Override
    public final void setConfig(final IConfiguration config) {
        this.config = config;
        // configure the blacklist of branches
        refresh();
    }

    @Override
    public final IConfiguration getConfiguration() {
        return this.config;
    }

    @Override
    public final void refresh() {
        // configure the blacklist of branches (configured in at least one environment)
        final Collection<IRepository> repositories = config.getRepositories();
        blackListedBranches = new HashMap<>(repositories.size());
        repositories.forEach(r -> blackListedBranches.put(r, new HashSet<>()));
        for (final IEnvironment env : config.getEnvironments()) {
            env.getEnvironmentBranchByRepository()
                    .forEach((repo, envBranch) -> blackListedBranches.get(repo).add(envBranch));
        }
    }

    @Override
    public final List<IFeatureBranch> getFeatureBranches(final IEnvironment environment) {
        final Set<IFeatureBranch> featureBranches = getDefaultProviderBranches(environment);
        featureBranches.addAll(doGetFeatureBranches(environment));
        return new ArrayList<>(featureBranches);
    }

    /**
     * Hook to include default provider branches before fetching them.
     * </br>
     * This is useful to add special branches, like in the manual branch provider.
     * 
     * @param environment environment to get the branches from.
     * 
     * @return a ordered set.
     */
    protected Set<IFeatureBranch> getDefaultProviderBranches(final IEnvironment environment) {
        return new LinkedHashSet<>();
    }

    private final List<IFeatureBranch> doGetFeatureBranches(final IEnvironment environment) {
        final Set<String> branches = new LinkedHashSet<>();
        for (final IRepository repo : environment.getRepositories()) {
            if (repo.isCloned()) {
                final List<String> filteredBranches = repo.getBranches().stream()//
                        .map(AbstractGitBranchProvider::normalizeBranchNames) // normalize names
                        .filter(name -> keepBranchName(name, environment)) // filter
                        .collect(Collectors.toList());
                // add to the branches
                branches.addAll(filteredBranches);
            }
        }

        return branches.stream() //
                .map(branch -> new GitFeatureBranch(this, environment, branch))//
                .collect(Collectors.toList());
    }

    private final boolean keepBranchName(final String branchName, final IEnvironment environment) {
        // 1. it is an internal branch
        if (branchName.startsWith(IEnvironment.INTERNAL_BRANCH_PREFIX)) {
            return false;
        }

        // 2. it is in the blacklist of branches for the repositories
        for (final IRepository repo : environment.getRepositories()) {
            if (blackListedBranches.get(repo).contains(branchName)) {
                return false;
            }
        }

        return doKeepBranch(branchName, environment);
    }

    protected abstract boolean doKeepBranch(final String branchName,
            final IEnvironment environment);

    private static final String normalizeBranchNames(final String name) {
        // remove the origin/ part of the branches
        return name.replaceFirst(GitConstants.ORIGIN + "/", "");
    }

    protected static class GitFeatureBranch implements IFeatureBranch {

        private final AbstractGitBranchProvider provider;
        private final IEnvironment environment;
        protected String branch;

        protected GitFeatureBranch(AbstractGitBranchProvider provider, IEnvironment environment,
                String branch) {
            this.provider = provider;
            this.environment = environment;
            this.branch = branch;
        }

        @Override
        public final IEnvironment getEnvironment() {
            return environment;
        }

        @Override
        public final IFeatureBranchProvider getProvider() {
            return provider;
        }

        @Override
        public final String getId() {
            return getName();
        }

        @Override
        public final String getName() {
            return branch.replace(
                    getEnvironment().getBranchPrefix() + IEnvironment.BRANCH_NAME_SEPARATOR, "");
        }

        @Override
        public String getDisplayName() {
            // return the branch name itself or the name from the provider (easier)
            return getFromMatchedProvider(IFeatureBranch::getName).orElse(getName());
        }

        @Override
        public final String getCheckoutBranch(final IRepository repository) {
            String checkoutBranchName = getName();
            final String envBranch = getEnvironment().getEnvironmentBranch(repository);
            // if it is the same as the environment branch
            if (envBranch.equals(checkoutBranchName)) {
                return getEnvironment().getGemInternalBranchName();
            }

            // overriden as the checkout branch is the same
            return branch;
        }

        @Override
        public final String getSummary() {
            // TODO - return empty summary name instead?
            return getFromMatchedProvider(IFeatureBranch::getDescription).orElse("No summary");
        }

        @Override
        public final String getDescription() {
            // TODO - return empty description instead??
            // no description unless it is commit from other provider
            return getFromMatchedProvider(IFeatureBranch::getDescription).orElse("No description");
        }

        @Override
        public final String getDefaultCommitMessage() {
            // no default commit message if it is not matching other provider
            return getFromMatchedProvider(IFeatureBranch::getDefaultCommitMessage).orElse("");
        }

        protected final Optional<String> getFromMatchedProvider(
                final Function<IFeatureBranch, String> stringFunction) {
            final Optional<IFeatureBranch> fb = getFbFromMatchedProvider();
            if (fb.isPresent()) {
                return Optional.of(stringFunction.apply(fb.get()));
            }
            return Optional.empty();
        }

        private final Optional<IFeatureBranch> getFbFromMatchedProvider() {
            // TODO - cache the matching branch?
            return provider.getConfiguration().getServiceContainer().getFeatureBranchProviders()
                    .stream()//
                    .filter(p -> !(p instanceof AbstractGitBranchProvider)) //
                    .map(fbp -> fbp.getMatchingFeatureBranch(environment, this).orElse(null))//
                    .filter(Objects::nonNull) //
                    .findFirst();
        }

        @Override
        public final List<ILauncher> getLaunchers() {
            final Optional<IFeatureBranch> fb = getFbFromMatchedProvider();
            if (fb.isPresent()) {
                return fb.get().getLaunchers();
            }
            return IFeatureBranch.super.getLaunchers();
        }

        @Override
        public List<ILauncher> getRepositoryLaunchers(final IRepository repository) {
            final Optional<IFeatureBranch> fb = getFbFromMatchedProvider();
            if (fb.isPresent()) {
                return fb.get().getRepositoryLaunchers(repository);
            }
            return IFeatureBranch.super.getRepositoryLaunchers(repository);
        }
        
        @Override
        public final int hashCode() {
            return getId().hashCode();
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof GitFeatureBranch) {
                final GitFeatureBranch other = (GitFeatureBranch) obj;
                return Objects.equals(branch, other.branch)
                        && Objects.equals(environment, other.environment)
                        && Objects.equals(provider, other.provider);
            }

            return false;
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
        if (obj instanceof IFeatureBranchProvider) {
            return compareTo((IFeatureBranchProvider) obj) == 0;
        }

        return false;
    }

}
