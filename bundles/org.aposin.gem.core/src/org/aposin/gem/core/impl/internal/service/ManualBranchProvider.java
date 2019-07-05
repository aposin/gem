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
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.launcher.IParam.StringParam;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.api.workflow.IFeatureBranchWorkflow;
import org.aposin.gem.core.api.workflow.WorkflowException;
import org.osgi.service.component.annotations.Component;

/**
 * Provider for feature branches that are created manually.
 * </br>
 * This provider creates branches on the environment-specific branch prefixed by
 * <code>{@link IEnvironment#getBranchPrefix()}/{@link #MANUAL_BRANCH_PROVIDER_NAME}/{@link IConfiguration#getManualBranchId()}</code>.
 * </br>
 * The first branch on the list is to create a new branch with the provided name.
 * Rest of the branches are either the ones generated or the ones already on the repository.
 */
@Component(service = IFeatureBranchProvider.class)
public class ManualBranchProvider extends AbstractGitBranchProvider {

    @Override
    public String getName() {
        return "MANUAL";
    }

    @Override
    public String getDisplayName() {
        return "Manually Generated (GEM)";
    }

    private final String getManualBranchPrefix(final IEnvironment environment) {
        return environment.getBranchPrefix() + IEnvironment.BRANCH_NAME_SEPARATOR
                + getConfiguration().getManualBranchId() + IEnvironment.BRANCH_NAME_SEPARATOR;
    }

    /**
     * Include also the manual branch.
     */
    @Override
    protected Set<IFeatureBranch> getDefaultProviderBranches(final IEnvironment environment) {
        final Set<IFeatureBranch> manual = new LinkedHashSet<>();
        manual.add(new ManualFeatureBranch(this, environment));
        return manual;
    }

    /**
     * Keep only manual branches.
     */
    @Override
    protected boolean doKeepBranch(final String branchName, final IEnvironment environment) {
        return branchName.startsWith(getManualBranchPrefix(environment));
    }

    /**
     * Manual branch implementation.
     */
    private final class ManualFeatureBranch extends GitFeatureBranch {

        private static final String PLACEHOLDER_SUFFIX = "{{branch.name}}";
        private final String placeholder;

        protected ManualFeatureBranch(final AbstractGitBranchProvider provider,
                final IEnvironment environment) {
            super(provider, environment, getManualBranchPrefix(environment) + PLACEHOLDER_SUFFIX);
            this.placeholder = branch;
        }

        @Override
        public String getDisplayName() {
            return "Create New Branch";
        }

        @Override
        public IFeatureBranchWorkflow getWorkflow() {
            final IFeatureBranchWorkflow gitBranchWorkflow = super.getWorkflow();

            return new IFeatureBranchWorkflow() {

                @Override
                public ManualFeatureBranch getFeatureBranch() {
                    return (ManualFeatureBranch) gitBranchWorkflow.getFeatureBranch();
                }

                @Override
                public ILauncher getPullLauncher(final Function<IWorktreeDefinition, Boolean> shouldAbort)
                        throws WorkflowException {
                    return new ManualBranchLauncherWrapper(
                            gitBranchWorkflow.getPullLauncher(shouldAbort),
                            this.getFeatureBranch());
                }

                @Override
                public ILauncher getMergeBaseIntoFeatureBranchLauncher(
                        final Function<IWorktreeDefinition, Boolean> doContinueProvider) throws WorkflowException {
                    return new ManualBranchLauncherWrapper(gitBranchWorkflow
                            .getMergeBaseIntoFeatureBranchLauncher(doContinueProvider),
                            this.getFeatureBranch());
                }

                @Override
                public ILauncher getFetchAndCheckoutLauncher() throws WorkflowException {
                    return new ManualBranchCheckoutLauncher(gitBranchWorkflow.getFetchAndCheckoutLauncher(),
                            this.getFeatureBranch());
                }

                @Override
                public ILauncher getCleanWorktreeLauncher() throws WorkflowException {
                    return new ManualBranchLauncherWrapper(gitBranchWorkflow
                            .getCleanWorktreeLauncher(),
                            this.getFeatureBranch());
                }
                
                @Override
                public ILauncher getRemoveBranchLauncher() throws WorkflowException {
                    return new ManualBranchCheckoutLauncher(
                            gitBranchWorkflow.getRemoveBranchLauncher(), this.getFeatureBranch());
                }
            };
        }
    }

    private static class ManualBranchLauncherWrapper implements ILauncher {

        protected final ILauncher delegate;
        protected final ManualFeatureBranch fb;

        public ManualBranchLauncherWrapper(final ILauncher delegateCheckout,
                final ManualFeatureBranch fb) {
            this.delegate = delegateCheckout;
            this.fb = fb;
        }

        @Override
        public final String getId() {
            return delegate.getName();
        }

        @Override
        public final String getName() {
            return delegate.getName();
        }

        @Override
        public String getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public final INamedObject getGroup() {
            return delegate.getGroup();
        }

        @Override
        public final INamedObject getLaunchScope() {
            return delegate.getLaunchScope();
        }

        /**
         * Can be overriden (by checkout).
         */
        @Override
        public boolean canLaunch() {
            if (fb.placeholder.equals(fb.branch)) {
                return false;
            }
            return delegate.canLaunch();
        }

        /**
         * Can be overriden (by checkout).
         */
        @SuppressWarnings("rawtypes")
        @Override
        public Set<IParam> createParams() throws GemException {
            return delegate.createParams();
        }

        /**
         * Can be overriden (by checkout).
         */
        @Override
        public boolean requireParams() {
            return delegate.requireParams();
        }

        /**
         * Can be overriden (by checkout).
         */
        @Override
        public List<ICommand> launch() throws GemException {
            return delegate.launch();
        }

        /**
         * Can be overriden (by checkout).
         */
        @SuppressWarnings("rawtypes")
        @Override
        public List<ICommand> launch(Set<IParam> params) throws GemException {
            return delegate.launch(params);
        }
    }

    private final class ManualBranchCheckoutLauncher extends ManualBranchLauncherWrapper {

        public ManualBranchCheckoutLauncher(final ILauncher delegateCheckout,
                final ManualFeatureBranch fb) {
            super(delegateCheckout, fb);
        }

        @Override
        public String getDisplayName() {
            return "Checkout Manual Branch";
        }

        @Override
        public boolean canLaunch() {
            return delegate.canLaunch();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Set<IParam> createParams() throws GemException {
            if (!fb.placeholder.equals(fb.branch)) {
                return delegate.createParams();
            }
            final Set<IParam> params = new LinkedHashSet<>();
            params.add(new StringParam() {

                @Override
                public String getName() {
                    return "branchname";
                }

                @Override
                public String getDisplayName() {
                    return "Branch Name";
                }

                @Override
                public boolean isValid(final String value) {
                    if (value == null || value.isBlank() || value.contains(" ")) {
                        return false;
                    }
                    return true;
                }

                @Override
                public boolean isRequired() {
                    return true;
                }
            });

            if (delegate.requireParams()) {
                params.addAll(delegate.createParams());
            }

            return params;
        }

        @Override
        public boolean requireParams() {
            return delegate.requireParams() || fb.placeholder.equals(fb.branch);
        }

        @Override
        public List<ICommand> launch() throws GemException {
            if (fb.placeholder.equals(fb.branch)) {
                return delegate.launch();
            }
            throw new GemException("Require Feature-Branch name");
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public List<ICommand> launch(Set<IParam> params) throws GemException {
            for (final IParam p : params) {
                if (p.getId().equals("branchname")) {
                    if (!p.isValid(p.getValue())) {
                        throw new GemException("Invalid " + p.getDisplayName() + ": " + "'" + p.getValue() + "'");
                    }

                    fb.branch = getManualBranchPrefix(fb.getEnvironment()) + p.getValue();
                }
            }
            if (delegate.requireParams()) {
                return delegate.launch(params);
            } else {
                return delegate.launch();
            }
        }
    }

}
