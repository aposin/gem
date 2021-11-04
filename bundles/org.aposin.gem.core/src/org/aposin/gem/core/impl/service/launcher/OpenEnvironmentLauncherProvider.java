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
package org.aposin.gem.core.impl.service.launcher;

import java.awt.Desktop;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IEnvironmentWorkflow;
import org.aposin.gem.core.exception.GemException;
import org.osgi.service.component.annotations.Component;

@Component(service = IEnvironmentLauncherProvider.class)
public class OpenEnvironmentLauncherProvider implements IEnvironmentLauncherProvider {

    /**
     * Name for the Open Worktree launcher.
     */
    public static final String OPEN_WORKTREE_LAUNCHER_NAME = "open_worktree";

    /**
     * Name for the Open Server launcher.
     */
    public static final String OPEN_SERVER_LAUNCHER_NAME = "open_server";

    /**
     * Group for the launchers created by this provider.
     */
    public static final INamedObject OPEN_GROUP = new INamedObject() {

        @Override
        public String getName() {
            return "open_environment";
        }

        @Override
        public String getDisplayName() {
            return "Open";
        }
    };

    private final Map<IRepository, ILauncher> openServerLaunchers = new HashMap<>();

    /**
     * {@inheritDoc}
     * 
     * @return {@link #NAME}
     */
    @Override
    public String getName() {
        return OPEN_GROUP.getName();
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@link #DISPLAY_NAME}
     */
    @Override
    public String getDisplayName() {
        return OPEN_GROUP.getDisplayName();
    }

    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        for (final IRepository repo : config.getRepositories()) {
            openServerLaunchers.put(repo, new OpenRepoServer(repo));
        }
    }

    @Override
    public List<ILauncher> getLaunchers(final IEnvironment environment) {
        return Collections.singletonList(new OpenWorktreesFolder(environment));
    }

    
    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IEnvironment environment) {
        final Map<IRepository, List<ILauncher>> repositoryLaunchers = new LinkedHashMap<>();
        for (final IRepository repository: environment.getRepositories()) {
            repositoryLaunchers.put(repository, Collections.singletonList(openServerLaunchers.get(repository)));
        }
        return repositoryLaunchers;
    }
    

    private static final class OpenWorktreesFolder extends AbstractNoParamsLauncher {

        private final IEnvironment environment;

        public OpenWorktreesFolder(final IEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public INamedObject getGroup() {
            return OPEN_GROUP;
        }

        @Override
        public String getName() {
            return OPEN_WORKTREE_LAUNCHER_NAME;
        }

        @Override
        public String getDisplayName() {
            // TODO - check if the environment have several or one worktree?
            return "Worktree(s)";
        }

        @Override
        public IEnvironment getLaunchScope() {
            return environment;
        }

        @Override
        public boolean canLaunch() {
            if (Desktop.isDesktopSupported()) {
                final IEnvironmentWorkflow workflow = getLaunchScope().getWorkflow();
                if (!(workflow.getCloneLauncher().canLaunch() || workflow.getSetupWorktreeLauncher().canLaunch())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<ICommand> launch() throws GemException {
            try {
                // TODO - check if only one repo, and then open directly there?
                Desktop.getDesktop().open(getLaunchScope().getWorktreesBaseLocation().toFile());
            } catch (final IOException e) {
                throw new GemException("Error opening environment worktree location: "
                        + environment.getDisplayName(), e);
            }

            return Collections.emptyList();
        }
    }

    private static final class OpenRepoServer extends AbstractNoParamsLauncher {

        public IRepository repo;

        @Override
        public INamedObject getGroup() {
            return OPEN_GROUP;
        }

        public OpenRepoServer(final IRepository repo) {
            this.repo = repo;
        }

        @Override
        public IRepository getLaunchScope() {
            return repo;
        }

        @Override
        public String getName() {
            return OPEN_SERVER_LAUNCHER_NAME;
        }

        @Override
        public String getDisplayName() {
            return "Server";
        }

        @Override
        public boolean canLaunch() {
            return Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        }

        @Override
        public List<ICommand> launch() throws GemException {
            try {
                Desktop.getDesktop().browse(repo.getServer());
            } catch (final IOException e) {
                throw new GemException("Error opening repository: " + repo, e);
            }

            return Collections.emptyList();
        }
    }

}
