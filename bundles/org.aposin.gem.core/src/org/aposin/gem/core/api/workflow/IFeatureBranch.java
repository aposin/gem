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

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;

/**
 * Represents a feature-branch for an environment.
 */
public interface IFeatureBranch extends INamedObject {

    /**
     * Separator used to construct path-like branch names.
     */
    public static final String SEPARATOR = "/";

    /**
     * Gets the environment where this feature-branch belongs to.
     * 
     * @return environment.
     */
    public IEnvironment getEnvironment();

    /**
     * Gets the ID for the feature-branch.
     * </br>
     * This ID could be shared by a feature-branch between environments,
     * but must be unique within environments.
     */
    @Override
    public String getId();

    /**
     * Gets the name for the feature-branch.
     * </br>
     * This name is used by {@link #getCheckoutBranch(IRepository)}
     * to construct the checkout branch.
     * </br>
     * Default implementation returns a path-like branch name using
     * <code>{@link IFeatureBranchProvider#getName()}/{@link #getId()}</code>
     * 
     * @return branch name.
     */
    @Override
    public default String getName() {
        return getProvider().getName() + IEnvironment.BRANCH_NAME_SEPARATOR + getId();
    }

    /**
     * Gets the display name for the feature-branch.
     * </br>
     * Default implementation returns
     * <code>[{@link #getId()}] {@link #getSummary()}</code>
     * 
     * @return display name for the feature-branch.
     */
    @Override
    public default String getDisplayName() {
        return MessageFormat.format("[{0}] {1}", //
                getId(), //
                getSummary());
    }

    /**
     * Gets the checkout branch for the repository.
     * </br>
     * Default implementation returns the {@link #getBranchName()} if it is not the same
     * as the {@link IEnvironment#getEnvironmentBranch(IRepository)}; otherwise,
     * returns {@link IEnvironment#getGemInternalBranchName()}.
     * </br>
     * Must not be overriden!
     * 
     * @param repository repository to checkout the feature branch.
     * @return name to checkout the branch (might be internal).
     * @nooverride
     */
    public default String getCheckoutBranch(final IRepository repository) {
        String checkoutBranchName = getName();
        final String envBranch = getEnvironment().getEnvironmentBranch(repository);
        // if it is the same
        if (envBranch.equals(checkoutBranchName)) {
            return getEnvironment().getGemInternalBranchName();
        }

        return getEnvironment().getBranchPrefix() + IEnvironment.BRANCH_NAME_SEPARATOR
                + checkoutBranchName;
    }

    /**
     * Gets the summary for the feature-branch.
     * 
     * @return summary.
     */
    public String getSummary();

    /**
     * Gets the description for the feature-branch.
     * 
     * @return description.
     */
    public String getDescription();

    /**
     * Gets the default commit message.
     * </br>
     * Default implementation returns a header line with the following format:
     * <code>[{@link #getId()}] {@link #getDescription()}</code>.
     * 
     * @return default commit message.
     */
    public default String getDefaultCommitMessage() {
        return MessageFormat.format("[{0}] {1}\n\n{2}", //
                getId(), //
                getSummary(), //
                getDescription());
    }

    /**
     * Gets the provider which generated this feature-branch.
     * 
     * @return provider
     */
    public IFeatureBranchProvider getProvider();

    /**
     * Gets the launcher for this feature branch.
     * </br>
     * Default implementation gets the launchers from the registered
     * {@link IFeatureBranchLauncherProvider#getLaunchers(IFeatureBranch)},
     * which should always being returned.
     * 
     * @return launchers; empty list if none.
     */
    public default List<ILauncher> getLaunchers() {
        return getEnvironment().getConfiguration().getServiceContainer()//
                .getFeatureBranchLauncherProviders().stream() //
                .flatMap(provider -> provider.getLaunchers(this).stream()) //
                .collect(Collectors.toList());
    }

    /**
     * Gets the launcher for this feature branch.
     * </br>
     * Default implementation gets the launchers from the registered
     * {@link IFeatureBranchLauncherProvider#getRepositoryLaunchers(IRepository)},
     * which should always being returned.
     * 
     * @return launchers; empty list if none.
     */
    public default List<ILauncher> getRepositoryLaunchers(final IRepository repository) {
        return  getEnvironment().getConfiguration().getServiceContainer()//
                .getFeatureBranchLauncherProviders().stream() //
                .flatMap(provider -> { //
                    final List<ILauncher> launchers = provider.getRepositoryLaunchers(this).get(repository); //
                    return launchers != null ? launchers.stream() : Stream.empty();
                }) //
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the workflow for this feature branch.
     * </br>
     * Default implementation returns the environment's 
     * ({@link IFeatureBranch#getEnvironment()}) workflow
     * ({@link IEnvironmentWorkflow#getFeatureBranchWorkflow(IFeatureBranch)}).
     * 
     * @return workflow for the feature branch.
     */
    public default IFeatureBranchWorkflow getWorkflow() {
        return getEnvironment().getWorkflow().getFeatureBranchWorkflow(this);
    }

}
