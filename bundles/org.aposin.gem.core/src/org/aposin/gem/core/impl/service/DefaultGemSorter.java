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
package org.aposin.gem.core.impl.service;

import java.util.Comparator;

import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.service.IGemSorter;

/**
 * Default {@link IGemSorter}.
 * </br>
 * This sorter can be extended by plug-ins and be registered as a service (only one is allowed).
 * </br>
 * On the constructor, sub-classes should register the core serice comparators.
 * If other services that are plug-in specific are provided, it can either be registered by the
 * plug-in itself {@link #registerServiceComparator(Class, Comparator)} or by the sorter if it is
 * application-specific.
 */
public class DefaultGemSorter implements IGemSorter, IConfigurable {

    private IConfiguration config;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setConfig(final IConfiguration config) throws GemConfigurationException {
        this.config = config;
        loadConfig();
    }

    /**
     * Hook-method for sub-classes, to load the configuration on {@link #setConfig(IConfiguration));
     */
    protected void loadConfig() {
        // NO-OP
    }

    @Override
    public final IConfiguration getConfiguration() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<IEnvironment> getEnvironmentComparator() {
        return Comparator.reverseOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<IProject> getProjectComparator() {
        return Comparator.naturalOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<IFeatureBranchProvider> getFeatureBranchProviderComparator() {
        return new Comparator<IFeatureBranchProvider>() {

            @Override
            public int compare(IFeatureBranchProvider o1, IFeatureBranchProvider o2) {
                final IFeatureBranchProvider defaultFBP = config.getServiceContainer()
                        .getDefaultFeatureBranchProvider();
                if (o1 == defaultFBP) {
                    return -1;
                } else if (o2 == defaultFBP) {
                    return 1;
                } else {
                    return o1.compareTo(o2);
                }
            }

        };
    }

}
