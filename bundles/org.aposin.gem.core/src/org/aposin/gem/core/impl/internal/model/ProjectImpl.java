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
package org.aposin.gem.core.impl.internal.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.exception.GemFatalException;
import org.aposin.gem.core.impl.internal.config.bean.GemCfgBean.EnvironmentBean;
import org.aposin.gem.core.impl.internal.config.bean.GemCfgBean.ProjectBean;
import org.aposin.gem.core.impl.internal.model.env.EnvironmentImpl;
import org.aposin.gem.core.impl.internal.model.env.ObsoleteKnownEnvironment;

/**
 * Internal implementation of {@link IProject}.
 */
public final class ProjectImpl implements IProject {

    private final IConfiguration config;
    private final ProjectBean project;
    private List<IEnvironment> environments = null;
    private List<IEnvironment> obsoleteEnvironments = null;

    /**
     * 
     * @param config
     * @param project
     */
    public ProjectImpl(final IConfiguration config, final ProjectBean project) {
        this.config = config;
        this.project = project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return project.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return project.displayname;
    }

    @Override
    public void refresh() {
        // only used by now for obsolete environments
        obsoleteEnvironments = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IEnvironment> getEnvironments() {
        if (environments == null) {
            loadEnvironments();
        }

        return environments;
    }

    @Override
    public List<IEnvironment> getObsoleteEnvironments() {
        if (obsoleteEnvironments == null) {
            loadObsoleteEnvironments();
        }
        return obsoleteEnvironments;
    }

    private void loadObsoleteEnvironments() {
        final String projectInternalBranch = IEnvironment.INTERNAL_BRANCH_PREFIX + getBranchPrefix()
                + IEnvironment.BRANCH_NAME_SEPARATOR;
        final Set<IRepository> processedRepos = new HashSet<>();
        final Map<String, Set<IRepository>> branchToRepos = new HashMap<>();
        final Set<String> envInternalBranches = new HashSet<>();
        final Set<String> maybeObsoleteBranches = new HashSet<>();
        for (final IEnvironment env : getEnvironments()) {
            envInternalBranches.add(env.getGemInternalBranchName());
            for (final IRepository repo : env.getRepositories()) {
                if (!processedRepos.contains(repo) && repo.isCloned()) {
                    final List<String> foundBranches = repo.getBranches().stream() //
                            .filter(b -> b.startsWith(projectInternalBranch)) //
                            .collect(Collectors.toList());
                    for (final String found : foundBranches) {
                        branchToRepos.compute(found, (k, repos) -> {
                            if (repos == null) {
                                repos = new HashSet<>();
                            }
                            repos.add(repo);
                            return repos;
                        });
                    }
                    maybeObsoleteBranches.addAll(foundBranches);
                }

                processedRepos.add(repo);
            }
        }
        maybeObsoleteBranches.removeAll(envInternalBranches);
        if (maybeObsoleteBranches.isEmpty()) {
            // no obsolete environments
            obsoleteEnvironments = Collections.emptyList();
        }
        // maybeObsoleteBranches only contain obsoleteBranches
        obsoleteEnvironments = maybeObsoleteBranches.stream() //
                .map(branch -> new ObsoleteKnownEnvironment(config, this, projectInternalBranch, branch, branchToRepos.get(branch))) //
                .sorted(config.getServiceContainer().getGemSorter().getEnvironmentComparator()) //
                .collect(Collectors.toList());
    }

    private void loadEnvironments() {
        environments = new ArrayList<>(project.environments.size());
        for (final EnvironmentBean env : project.environments) {
            // check that all repos are defined!
            for (final String repoId : env.branches.keySet()) {
                if (config.getRepository(repoId) == null) {
                    // TODO - make specific exception for this or hook to allow to continue (so load
                    // the rest but show that this is wrong)
                    throw new GemFatalException(MessageFormat.format(
                            "Repository ''{0}'' for environment ''{1}@{2}'' not defined", repoId,
                            project.name, env.name));
                }
            }
            environments.add(new EnvironmentImpl(config, this, env));
        }
        // sort environments using the sorter extension
        environments.sort(config.getServiceContainer().getGemSorter().getEnvironmentComparator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IProject) {
            return compareTo((IProject) obj) == 0;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ":" + project.toString();
    }

}
