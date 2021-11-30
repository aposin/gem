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
package org.aposin.gem.core.api.service;

import java.util.List;
import java.util.Optional;

import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.workflow.IFeatureBranch;

/**
 * Service to provide feature-branches for environments.
 */
public interface IFeatureBranchProvider extends IGemService, IRefreshable {

    /**
     * ID prefix to identify feature-branch providers provided by GEM Core.
     * </br>
     * IMPORTANT NOTE: should only be used on {@link #getId()} from plug-ins provided
     * by the GEM Core (including extra plug-ins).
     */
    public static final String CORE_ID_PREFIX = "org.aposin.gem.fbp.";

    /**
     * Get the feature-branches for the environment.
     * 
     * @param environment the environment to get the branches for.
     * @return feature-branches for the environment; empty list if no feature-branches are provided for the environment.
     */
    public List<IFeatureBranch> getFeatureBranches(final IEnvironment environment);

    /**
     * Gets the default feature-branch for the environment.
     * </br>
     * Default implementation returns the first value on
     * {@link #getFeatureBraches(IEnvironment, boolean)}.
     * Implementors might use a more efficient implementation.
     * 
     * @param environment the environment to get the branch for.
     * @return default branch for the environment; empty if {@link #getFeatureBranches(IEnvironment, boolean)} returns an empty list.
     */
    public default Optional<IFeatureBranch> getDefaultFeatureBranch(
            final IEnvironment environment) {
        final List<IFeatureBranch> branches = getFeatureBranches(environment);
        if (branches.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(branches.get(0));
    }

    /**
     * Gets a matching feature-branch.
     * </br>
     * Matching feature-branches are provider dependent.
     * Default implementation tries to find a feature-branch named
     * ({@link IFeatureBranch#getName()}) in the same way as the whole set of
     * {@link #getFeatureBraches(IEnvironment)}
     * 
     * @param environment the environment to get the branch for.
     * @param featureBranch feature-branch to match; if {@code null} no matching branch.
     * @return optional matching branch; {@link Optional#empty()} if not found.
     */
    public default Optional<IFeatureBranch> getMatchingFeatureBranch(final IEnvironment environment,
            final IFeatureBranch featureBranch) {
        if (featureBranch == null) {
            return Optional.empty();
        }
        return getFeatureBranches(environment).stream() //
                .filter(wb -> wb.getName().equals(featureBranch.getName())) //
                .findFirst();
    }

}
