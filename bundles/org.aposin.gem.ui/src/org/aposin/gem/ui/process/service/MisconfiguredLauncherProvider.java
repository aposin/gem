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
package org.aposin.gem.ui.process.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.service.IGemService;
import org.aposin.gem.core.api.service.IServiceContainer;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.eclipse.jface.dialogs.MessageDialog;
import org.osgi.service.component.annotations.Component;

/**
 * Provider to show a placeholder if no service is configured or a button to open
 * an error dialog if there is some misconfiguration issue for the service.
 */
@Component(service = {IEnvironmentLauncherProvider.class, IFeatureBranchLauncherProvider.class})
public class MisconfiguredLauncherProvider
        implements IEnvironmentLauncherProvider, IFeatureBranchLauncherProvider {

    /**
     * Launcher name for miscondifured launcher.
     */
    public static final String LAUNCHER_NAME = "misconfigured";

    private static final ILauncher NONE_LAUNCHER = new AbstractNoParamsLauncher() {

        @Override
        public String getId() {
            return LAUNCHER_NAME + "_none";
        }

        @Override
        public String getName() {
            return LAUNCHER_NAME;
        }

        @Override
        public String getDisplayName() {
            // TODO - externalize
            return "None configured";
        }

        @Override
        public List<ICommand> launch() throws GemException {
            throw new GemException("Not supposed to be launch");
        }

        @Override
        public INamedObject getLaunchScope() {
            return this;
        }

        @Override
        public INamedObject getGroup() {
            return new INamedObject() {

                @Override
                public String getName() {
                    return "";
                }

                @Override
                public String getDisplayName() {
                    return "";
                }
            };
        }

        @Override
        public boolean canLaunch() {
            return false;
        }
    };

    private IConfiguration config;

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        this.config = config;
    }

    @Override
    public List<ILauncher> getLaunchers(final IEnvironment environment) {
        return getMisconfiguredLaunchers(IEnvironmentLauncherProvider.class);
    }
    
    @Override
    public List<ILauncher> getLaunchers(final IFeatureBranch featureBranch) {
        return getMisconfiguredLaunchers(IFeatureBranchLauncherProvider.class, IFeatureBranchProvider.class);
    }
    
    private List<ILauncher> getMisconfiguredLaunchers(final Class<? extends IGemService> mainType, final Class<? extends IGemService>... otherTypes) {
        final List<ILauncher> launchers = new ArrayList<ILauncher>();
        final IServiceContainer serviceContainer = config.getServiceContainer();
        populateMisconfiguredLaunchers(mainType, serviceContainer, launchers);
        for (final Class<? extends IGemService> type : otherTypes) {
            populateMisconfiguredLaunchers(type, serviceContainer, launchers);
        }

        // first check the misconfigured services
        if (launchers.isEmpty() //
                && serviceContainer.getGemServices(mainType).size() == 1 //
                && serviceContainer.getGemServices(mainType).contains(this)) {
            return Collections.singletonList(NONE_LAUNCHER);
        }
        return Collections.unmodifiableList(launchers);
    }

    private void populateMisconfiguredLaunchers(final Class<? extends IGemService> type,
                                                final IServiceContainer serviceContainer,
                                                final List<ILauncher> accumulator) {
        // check first the misconfigured creators for the service itself
        serviceContainer.getMisconfiguredServices(type).entrySet().stream() //
                .map(entry -> new MisconfiguredLauncher(entry.getKey(), entry.getValue())) //
                .forEach(accumulator::add);
    }

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IEnvironment environment) {
        // TODO - hook it here?
        return Collections.emptyMap();
    }
    
    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IFeatureBranch featureBranch) {
        // TODO - hook it here?
        return Collections.emptyMap();
    }
    
    private static class MisconfiguredLauncher extends AbstractNoParamsLauncher {

        private final IGemService service;
        private final GemConfigurationException error;

        public MisconfiguredLauncher(final IGemService service,
                final GemConfigurationException error) {
            this.service = service;
            this.error = error;
        }

        @Override
        public INamedObject getGroup() {
            return new INamedObject() {

                @Override
                public String getName() {
                    return "misconfigured";
                }

                @Override
                public String getDisplayName() {
                    // TODO - externalize
                    return "Misconfigured";
                }
            };
        }

        @Override
        public INamedObject getLaunchScope() {
            return service;
        }

        @Override
        public boolean canLaunch() {
            return true;
        }

        @Override
        public List<ICommand> launch() throws GemException {
            MessageDialog.openError(null, getDisplayName(), // TODO: externalize
                    MessageFormat.format("Misconfigured {0}\n\n{1}", //
                            getDisplayName(), //
                            error.getLocalizedMessage())); //
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return LAUNCHER_NAME;
        }

        @Override
        public String getDisplayName() {
            return service.getDisplayName();
        }

    }

}
