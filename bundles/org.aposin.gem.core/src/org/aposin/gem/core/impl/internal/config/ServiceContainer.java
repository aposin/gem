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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.aposin.gem.core.Activator;
import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.service.IGemService;
import org.aposin.gem.core.api.service.IGemServiceCreator;
import org.aposin.gem.core.api.service.IServiceContainer;
import org.aposin.gem.core.impl.internal.service.GemGitBranchProvider;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceContainer implements IServiceContainer, IConfigurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceContainer.class);

    private final ConfigurationImpl configuration;

    // track all services
    private final Set<IRefreshable> refreshableServices = new HashSet<>();
    // track services creators (configured or misconfigured)
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends IGemService>, List<IGemServiceCreator>> serviceCreators = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends IGemService>, Map<? extends IGemServiceCreator, GemConfigurationException>> misconfiguredServiceCreators = new HashMap<>();
    // track loaded/misconfigured services
    private final Map<Class<? extends IGemService>, Map<String, ? extends IGemService>> loadedServices =
            new HashMap<>();
    private final Map<Class<? extends IGemService>, Map<? extends IGemService, GemConfigurationException>> misconfiguredServices =
            new HashMap<>();

    private List<IFeatureBranchProvider> featureBranchProviders;

    /* package */ ServiceContainer(final ConfigurationImpl configuration) {
        this.configuration = configuration;
        loadServiceCreators();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void loadServiceCreators() {
        serviceCreators.clear();
        try {
            final Collection<ServiceReference<IGemServiceCreator>> serviceRefs = Activator.getBundleContext()
                    .getServiceReferences(IGemServiceCreator.class, null);
            for (final ServiceReference<IGemServiceCreator> s : serviceRefs) {
                final IGemServiceCreator serviceCreator = Activator.getBundleContext().getService(s);
                serviceCreators.compute(serviceCreator.getType(), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(serviceCreator);
                    if (serviceCreator instanceof IRefreshable) {
                        refreshableServices.add((IRefreshable) serviceCreator);
                    }
                    return v;
                });
            }
        } catch (final InvalidSyntaxException e) {
            LOGGER.error("Error loading service creators", e);
        }
    }

    @Override
    public IConfiguration getConfiguration() {
        return configuration;
    }


    @Override
    public IFeatureBranchProvider getDefaultFeatureBranchProvider() {
        IFeatureBranchProvider provider = getService(IFeatureBranchProvider.class,
                configuration.config.defaultfeaturebranchprovider);
        if (provider == null) {
            LOGGER.warn("Default provider not configured: using GEM Core default");
            provider = getService(IFeatureBranchProvider.class, GemGitBranchProvider.ID);
        }
        return provider;
    }

    @Override
    public Collection<IFeatureBranchProvider> getFeatureBranchProviders() {
        if (featureBranchProviders == null) {
            featureBranchProviders = getGemServices(IFeatureBranchProvider.class).stream() //
                    .sorted(getGemSorter().getFeatureBranchProviderComparator()) //
                    .collect(Collectors.toUnmodifiableList());
        }
        return featureBranchProviders;
    }

    @Override
    public <T extends IGemService> T getService(Class<T> type, String id) {
        return getGemServicesById(type).get(id);
    }

    @Override
    public <T extends IGemService> Collection<T> getGemServices(final Class<T> type) {
        return Collections.unmodifiableCollection(getGemServicesById(type).values());
    }
    
    @SuppressWarnings("unchecked")
    private <T extends IGemService> Map<String, T> getGemServicesById(final Class<T> type) {
        return (Map<String, T>) loadedServices.computeIfAbsent(type, this::loadService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IGemService> Map<IGemService, GemConfigurationException> getMisconfiguredServices(
            final Class<T> type) {
        // triggers the loading (if not yet done)
        getGemServices(type);
        final Map<IGemService, GemConfigurationException> allMisconfigurations = //
                new HashMap<>(misconfiguredServiceCreators.get(type));
        allMisconfigurations.putAll(misconfiguredServices.get(type));

        return Collections.unmodifiableMap(allMisconfigurations);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T extends IGemService> Map<String, T> loadService(final Class<T> type) {
        LOGGER.info("Loading {} service(s)", type.getSimpleName());
        Map<String, T> configuredServices = new TreeMap<>();
        Map<T, GemConfigurationException> misconfigured = new TreeMap<>();
        Map<IGemServiceCreator<? extends IGemService>, GemConfigurationException> misconfiguredCreators = new TreeMap<>();
        try {
            final Collection<ServiceReference<T>> serviceRefs =
                    Activator.getBundleContext().getServiceReferences(type, null);
            for (final ServiceReference<T> s : serviceRefs) {
                final T service = Activator.getBundleContext().getService(s);
                configureServiceAndTrack(service, configuredServices, misconfigured);
            }
        } catch (final InvalidSyntaxException e) {
            LOGGER.error("Error loading service: " + type, e);
        }

        final List<IGemServiceCreator> creators = serviceCreators.getOrDefault(type,
                Collections.emptyList());
        for (final IGemServiceCreator<? extends IGemService> sCreator : creators) {
            if (configureService(sCreator, misconfiguredCreators)) {
                for (final IGemService createdService : sCreator.createServices()) {
                    configureServiceAndTrack((T) createdService, configuredServices, misconfigured);
                }
            }
        }

        if (configuredServices.isEmpty()) {
            LOGGER.error("No {} service configured", type.getSimpleName());
        }

        misconfiguredServices.put(type, misconfigured);
        misconfiguredServiceCreators.put(type, misconfiguredCreators);

        return configuredServices;
    }

    private <T extends IGemService> void configureServiceAndTrack(T service, Map<String, T> configured,
                                                                  final Map<T, GemConfigurationException> misconfigured) {
        if (configureService(service, misconfigured)) {
            configured.put(service.getId(), service);
        }
    }

    private <T extends IGemService> boolean configureService(T service,
                                                             final Map<T, GemConfigurationException> misconfigured) {
        LOGGER.info("Found service: {}", service.getId());
        if (service instanceof IRefreshable) {
            refreshableServices.add((IRefreshable) service);
        }
        try {
            service.setConfig(getConfiguration());
        } catch (final GemConfigurationException e) {
            misconfigured.put(service, e);
            LOGGER.error("Miss-configured service {}: ignored", service.getId());
            LOGGER.debug("Exception", e);
            return false;
        }
        return true;
    }

    @Override
    public void refresh() {
        // refresh the services that are refreshable
        refreshableServices.forEach(IRefreshable::refresh);
        // reload the service creators
        loadServiceCreators();
        // clean the cached services
        refreshableServices.clear();
        loadedServices.clear();
        misconfiguredServices.clear();
        misconfiguredServiceCreators.clear();
        featureBranchProviders = null;
    }
}
