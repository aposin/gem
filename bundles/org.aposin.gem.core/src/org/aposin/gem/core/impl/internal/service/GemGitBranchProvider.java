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
package org.aposin.gem.core.impl.internal.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.osgi.service.component.annotations.Component;

@Component(service = IFeatureBranchProvider.class)
public class GemGitBranchProvider extends AbstractGitBranchProvider {

    /**
    * ID for this provider, which is also the default Core provider.
    */
    public static final String ID = GemGitBranchProvider.class.getName();

    /**
    * {@inheritDoc}
    *
    * @return {@link #ID}
    */
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "gem_branches";
    }

    @Override
    public String getDisplayName() {
        return "Repository Branches (GEM)";
    }

    /**
     * Include the also the environment branches.
     */
    @Override
    protected Set<IFeatureBranch> getDefaultProviderBranches(final IEnvironment environment) {
        return environment.getEnvironmentBranchByRepository()//
                .values().stream() //
                .map(branch -> new GitFeatureBranch(this, environment, branch)) //
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    protected boolean doKeepBranch(String branchName, IEnvironment environment) {
        return branchName.startsWith(environment.getBranchPrefix());
    }

}
