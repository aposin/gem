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
package org.aposin.gem.core.api.config;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.IServiceContainer;

/**
 * Interface to configure the core of GEM.
 */
public interface IConfiguration extends IConfigurable {

    /**
     * Folder name for the repositories.
     */
    public static final String REPOSITORY_FOLDER_NAME = "repos";

    /**
     * {@inheritDoc}}
     * 
     * @noimplement
     */
    @Override
    public default IConfiguration getConfiguration() {
        return this;
    }

    public Path getRelativeToConfigFile(final String relativePath);

    /**
     * Gets the preferences.
     * 
     * @return
     */
    public IPreferences getPreferences();

    /**
     * Reloads the configuration.
     * 
     * @throws GemException if the configuration cannot be reload.
     */
    public void refresh() throws GemException;

    /**
     * Gets the resources directory where repositories, workspaces,
     * etc. will be stored.
     * 
     * @return resources directory.
     */
    public Path getResourcesDirectory();

    /**
     * Gets the branch ID for the manual branch provider.
     * 
     * @return manual branch ID.
     */
    public String getManualBranchId();

    /**
     * Gets where the repositories will be stored.
     * </br>
     * Default implementation returns the {@link #REPOSITORY_FOLDER_NAME}
     * on the {@link #getResourcesDirectory()}.
     * 
     * @return path for repositories.
     */
    public default Path getRepositoriesDirectory() {
        return getResourcesDirectory().resolve(REPOSITORY_FOLDER_NAME);
    }

    /**
     * Gets the temp directory for GEM.
     * </br>
     * Resources on this directory might be deleted on exit or
     * being not accessible outside of a method.
     * 
     * @return temp directory for GEM.
     */
    public Path getGemTempDirectory();

    /**
     * Gets the collection of repositories defined in the configuration.
     * 
     * @return set of repositories.
     */
    public Collection<IRepository> getRepositories();

    /**
     * Gets the repository by ID.
     * 
     * @param id if of the repository.
     * @return repository; {@code null} if the repository ID does not exists.
     */
    public IRepository getRepository(final String id);

    /**
     * Gets the set of projects defined in the configuration.
     * 
     * @return set of projects (as list).
     */
    public List<IProject> getProjects();

    /**
     * Get all environments defined in the configuration from all projects.
     * 
     * @return set of projects (as list).
     */
    public List<IEnvironment> getEnvironments();

    /**
     * Gets the service container.
     * 
     * @return the service container.
     */
    public IServiceContainer getServiceContainer();

    /**
     * Gets the configuration for any Plug-in extending the core functionality.
     * 
     * @param id identifier for the Plug-in configuration
     * @param configBean object representing the configured options
     * 
     * @return bean with the loaded configuration.
     * @throws GemConfigurationException if the configuration is missing or invalid.
     */
    public <T> T getPluginConfiguration(final String id, final Class<T> configBean)
            throws GemConfigurationException;

}
