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
package org.aposin.gem.core.impl.internal.workflow;

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.api.workflow.IFeatureBranchWorkflow;
import org.aposin.gem.core.api.workflow.WorkflowException;

public final class ObsoleteEnvironmentWorkflow extends AbstractGemWorkflow {

    public ObsoleteEnvironmentWorkflow(final IEnvironment obsoleteEnvironment) {
        super(obsoleteEnvironment);
    }

    @Override
    protected boolean requiresWorktreeSetup() {
        // obsolete environment shouldn't create a worktree
        return false;
    }

    @Override
    public boolean requiresClone() {
        // obsolete environemntes shouldn't be cloned
        return false;
    }

    @Override
    public boolean removeWorktreeIsEnabled() {
        // remove worktrees is always enabled
        return true;
    }

    @Override
    public IFeatureBranchWorkflow getFeatureBranchWorkflow(IFeatureBranch featureBranch) throws WorkflowException {
        throw new WorkflowException("Obsolete environemnt cannot manage feature branches!");
    }

}
