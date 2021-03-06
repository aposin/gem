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
package org.aposin.gem.core.impl.internal.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aposin.gem.core.api.config.ConfigConstants;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.config.prefs.IPreferences;
import org.aposin.gem.core.api.config.provider.IConfigFileProvider;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;
import org.aposin.gem.core.api.service.IServiceContainer;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.core.exception.GemFatalException;
import org.aposin.gem.core.impl.internal.config.bean.GemCfgBean;
import org.aposin.gem.core.impl.internal.config.bean.GemCfgBean.RepositoryBean;
import org.aposin.gem.core.impl.internal.config.prefs.PreferencesImpl;
import org.aposin.gem.core.impl.internal.model.ProjectImpl;
import org.aposin.gem.core.impl.internal.model.repo.RepositoryImpl;
import org.aposin.gem.core.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal implementation of {@link IConfiguration}.
 */
public final class ConfigurationImpl implements IConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationImpl.class);

    private final HoconFilesManager hoconFileManager;

    // reloaded
    private IServiceContainer services;
    private PreferencesImpl prefs;
    /*package*/ GemCfgBean config;

    private List<IProject> projects;
    private Map<String, IRepository> repositoriesById;

    /**
     * Default constructor.
     * 
     * @param configFileProvider
     * @param prefs
     */
    public ConfigurationImpl(final IConfigFileProvider configFileProvider) {
        this.services = new ServiceContainer(this);
        this.hoconFileManager = new HoconFilesManager(configFileProvider);
        refresh();
    }

    @Override
    public IPreferences getPreferences() {
        if (prefs == null) {
            prefs = new PreferencesImpl(hoconFileManager);
        }
        return prefs;
    }

    @Override
    public void refresh() throws GemException {
        projects = null;
        repositoriesById = null;
        // first the files should be read again
        hoconFileManager.refresh();
        // refresh the preferences if already created
        if (prefs != null) {
            prefs.refresh();
        }
        try {
            config = getPluginConfiguration(ConfigConstants.GEM_CONFIGURATION_ID, GemCfgBean.class);
        } catch (final GemConfigurationException e) {
            // convert any configuration exception to a fatal exception on gem-config loading
            throw new GemFatalException(e.getLocalizedMessage(), e);
        }
        // services should refresh at the end, once the configuration is reloaded
        services.refresh();
    }

    @Override
    public <T> T getPluginConfiguration(String id, Class<T> configBean) {
        return hoconFileManager.getConfigurationBean(getPreferences(), id, configBean);
    }

    @Override
    public Path getResourcesDirectory() {
        return Paths.get(config.resourcesdirectory).toAbsolutePath();
    }

    @Override
    public String getManualBranchId() {
        return config.manualbranchid;
    }

    // hooks for deletion of tmp directories
    // it is a map as the resources directory could be modified in the configuration
    private Map<Path, Thread> gemTmpDirDeletionHooks = new HashMap<>(1);

    /**
     * Gets the temp directory on the resources directory.
     * </br>
     * This creates the file if it does not exists, and also register it for
     * deletion on shootdown.
     */
    @Override
    public Path getGemTempDirectory() {
        final Path path = getResourcesDirectory().resolve(".tmp");
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                throw new GemException("Cannot create GEM temp directory");
            }
        }
        if (!gemTmpDirDeletionHooks.containsKey(path)) {
            final Thread hook = new Thread(new DeleteTempDirectoryHook(path), //
                    "Delete-" + path + " (shutdown)");
            Runtime.getRuntime().addShutdownHook(hook);
            LOGGER.debug("Registered for deletion on shutdown: {}", path);
            gemTmpDirDeletionHooks.put(path, hook);
        }
        return path;
    }

    private static final class DeleteTempDirectoryHook implements Runnable {

        private final Path tmpPath;

        private DeleteTempDirectoryHook(final Path tmpPath) {
            this.tmpPath = tmpPath;
        }

        @Override
        public void run() {
            // prevent files created on a new app instance to be deleted
            try (final Stream<Path> currentSiblings = Files.list(this.tmpPath)) {
                currentSiblings.forEach(p -> {
                    LOGGER.debug("Deleting {}", p);
                    IOUtils.deleteRecursivelyIgnoringErrors(p);
                });
            } catch (final Exception e) {
                // catch any exception to avoid failing hook
                LOGGER.error("Cannot delete files on " + this.tmpPath, e);
            }
        }
    }

    @Override
    public List<IProject> getProjects() {
        if (projects == null) {
            loadProjects();
        }
        return projects;
    }

    private void loadProjects() {
        projects = config.projects.stream() //
                .map(project -> new ProjectImpl(this, project)) //
                .sorted(getServiceContainer().getGemSorter().getProjectComparator()) //
                .collect(Collectors.toList());
    }

    @Override
    public List<IEnvironment> getEnvironments() {
        return getProjects().stream() //
                .flatMap(p -> p.getEnvironments().stream()) //
                .collect(Collectors.toList());
    }

    @Override
    public Collection<IRepository> getRepositories() {
        loadRepositories();
        return repositoriesById.values();
    }

    @Override
    public IRepository getRepository(final String repoId) {
        loadRepositories();
        return repositoriesById.get(repoId);
    }

    // load methods

    private void loadRepositories() {
        if (repositoriesById == null) {
            repositoriesById = new TreeMap<>();
            final List<CompletableFuture<IResult>> hookInstallers =
                    new ArrayList<>(config.repositories.size());
            for (final RepositoryBean repoInfo : config.repositories) {
                final RepositoryImpl repo = new RepositoryImpl(this, repoInfo);
                // if it is cloned, the first time that is instantiated
                // then install the hooks -> refresh or startup
                if (repo.isCloned()) {
                    final List<GemRepoHookDescriptor> hooks = repo.getHooks();
                    if (!hooks.isEmpty()) {
                        hookInstallers.add(
                                repo.getCommandBuilder().buildInstallHooksCommand(hooks).execute());
                    }

                }
                repositoriesById.put(repoInfo.id, repo);
            }
            final CompletableFuture<Void> hookCompleted =
                    CompletableFuture.allOf(hookInstallers.toArray(CompletableFuture[]::new));
            try {
                // force finished all
                hookCompleted.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new GemFatalException("Error installing repo-hooks", e);
            }
        }
    }

    @Override
    public Path getRelativeToConfigFile(final String relativePath) {
        return hoconFileManager.getConfigFileProvider().getRelativeToConfigFile(relativePath);
    }

    @Override
    public IServiceContainer getServiceContainer() {
        return services;
    }
}
