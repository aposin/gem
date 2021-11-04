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
package org.aposin.gem.core.api.service;

import java.util.Collection;
import java.util.Map;

import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.core.impl.service.DefaultGemSorter;

/**
 * Container class for the services implemented by core and/or extensions.
 */
public interface IServiceContainer extends IRefreshable, IConfigurable {

    /**
     * Gets the configured {@link IGemSorter}.
     * </br>
     * Default implementation checks for an optional unique gem-sorter.
     * 
     * @return sorter.
     * 
     * @throws GemException if more than one sorter is provided.
     */
    public default IGemSorter getGemSorter() {
        final Collection<IGemSorter> sorters = getGemServices(IGemSorter.class);
        switch (sorters.size()) {
            case 0:
                return new DefaultGemSorter();
            case 1:
                return sorters.iterator().next();
            default:
                throw new GemException("Several gem-sorters provided");
        }
    }

    /**
     * Gets the configured default feature branch provider.
     * 
     * @return default branch provider.
     */
    public IFeatureBranchProvider getDefaultFeatureBranchProvider();

    /**
     * Retrieve the {@link IFeatureBranchProvider} core service.
     * 
     * @return {@link #getService(Class)} for {@link IFeatureBranchProvider}.
     */
    public default Collection<IFeatureBranchProvider> getFeatureBranchProviders() {
        return getGemServices(IFeatureBranchProvider.class);
    }

    /**
     * Retrieve the {@link IEnvironmentLauncherProvider} core service.
     */
    public default Collection<IEnvironmentLauncherProvider> getEnvironmentLauncherProviders() {
        return getGemServices(IEnvironmentLauncherProvider.class);
    }

    /**
     * Gets a map of ID and properly configured services (core or not).
     * Retrieves the {@link IFeatureBranchLauncherProvider} core service.
     * 
     * @return collection of feature branch launcher provider.
     */
    public default Collection<IFeatureBranchLauncherProvider> getFeatureBranchLauncherProviders() {
        return getGemServices(IFeatureBranchLauncherProvider.class);
    }

    /**
     * Gets the service from a corresponding class and its ID.
     * 
     * @param <T> the type of the service.
     * @param type class for the service.
     * @param id id for the service.
     * @return the service.
     */
    public <T extends IGemService> T getService(final Class<T> type, final String id);

    /**
     * Gets a collection of the services from a concrete class.
     * </br>
     * The misconfigured services are not returned, but they can be
     * retrieved by {@link #getMisconfiguredServices(Class)}.
     * 
     * @param <T> the type of the service.
     * @param type class for the service.
     * @return map of the service ID plus the configured implementation.
     */
    public <T extends IGemService> Collection<T> getGemServices(final Class<T> type);

    /**
     * Gets a map of misconfigured services and creators and the exception causing it.
     * 
     * @param <T> the type of the service.
     * @param type class for the service.
     * @return map of the misconfigured service (or its creator) and the exception.
     */
    public <T extends IGemService> Map<IGemService, GemConfigurationException> getMisconfiguredServices(final Class<T> type);

}
