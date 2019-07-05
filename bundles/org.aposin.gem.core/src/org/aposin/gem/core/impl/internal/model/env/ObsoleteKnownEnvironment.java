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
package org.aposin.gem.core.impl.internal.model.env;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.workflow.IEnvironmentWorkflow;
import org.aposin.gem.core.impl.internal.workflow.ObsoleteEnvironmentWorkflow;

public class ObsoleteKnownEnvironment extends AbstractEnvironment {

    private final String projectInternalBranchPrefix;
    private final String gemInternalBranchName;
    private final List<IRepository> repos;

    public ObsoleteKnownEnvironment(final IConfiguration config, //
            final IProject project, //
            final String projectInternalBranchPrefix, //
            final String gemInternalBranchName, // 
            final Set<IRepository> repos) {
        super(config, project);
        this.projectInternalBranchPrefix = projectInternalBranchPrefix;
        this.gemInternalBranchName = gemInternalBranchName;
        this.repos = new ArrayList<>(repos);
    }

    @Override
    public String getGemInternalBranchName() {
        return gemInternalBranchName;
    }

    @Override
    public String getName() {
        return gemInternalBranchName.replaceFirst(projectInternalBranchPrefix, "");
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public List<IRepository> getRepositories() {
        return repos;
    }

    @Override
    public Map<IRepository, String> getEnvironmentBranchByRepository() {
        throw new GemException("Obsolete environment cannot find by-environemnt branches");
    }

    @Override
    public IEnvironmentWorkflow getWorkflow() {
        return new ObsoleteEnvironmentWorkflow(this);
    }

}
