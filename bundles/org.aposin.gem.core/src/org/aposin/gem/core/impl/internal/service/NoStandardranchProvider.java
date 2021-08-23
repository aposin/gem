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

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provider returning the branches on the environment repositories
 * which aren't part of other configuration.
 */
@Component(service = IFeatureBranchProvider.class)
public class NoStandardranchProvider extends AbstractGitBranchProvider {

    @Override
    public String getName() {
        return "non_gem";
    }

    @Override
    public String getDisplayName() {
        return "Repository (no-standard)";
    }

    @Override
    protected boolean doKeepBranch(final String branchName, final IEnvironment environment) {
        // if any match a prefix from an environment, then it does not show it
        return getConfiguration().getEnvironments().stream() //
                .noneMatch(env -> branchName.startsWith(env.getBranchPrefix()));
    }

}
