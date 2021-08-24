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
package org.aposin.gem.core.impl.internal.model.env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.workflow.IEnvironmentWorkflow;
import org.aposin.gem.core.impl.internal.config.bean.GemCfgBean.EnvironmentBean;
import org.aposin.gem.core.impl.internal.workflow.GemDefaultWorkflow;

/**
 * Internal implementation of {@link IEnvironment}.
 */
public final class EnvironmentImpl extends AbstractEnvironment {

    private final EnvironmentBean environment;

    private IEnvironmentWorkflow workflow;

    /**
     * 
     * @param config
     * @param project
     * @param environment
     */
    public EnvironmentImpl(final IConfiguration config, final IProject project,
            final EnvironmentBean environment) {
        super(config, project);
        this.environment = environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return environment.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return environment.displayname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IRepository> getRepositories() {
        return environment.branches.keySet().stream() //
                .map(getConfiguration()::getRepository)//
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<IRepository, String> getEnvironmentBranchByRepository() {
        final Map<IRepository, String> repoInfo = new HashMap<>(environment.branches.size());
        for (final Map.Entry<String, Object> branchDef : environment.branches.entrySet()) {
            repoInfo.put(getConfiguration().getRepository(branchDef.getKey()), (String) branchDef.getValue());
        }
        return repoInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEnvironmentWorkflow getWorkflow() {
        if (workflow == null) {
            this.workflow = new GemDefaultWorkflow(this);
        }
        return workflow;
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
        if (obj instanceof IEnvironment) {
            return compareTo((IEnvironment) obj) == 0;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ":" + environment.toString();
    }
}
