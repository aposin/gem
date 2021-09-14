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
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.service.IGemSorter;

/**
 * Default {@link IGemSorter}.
 * </br>
 * This sorter can be extended by plug-ins and be registered as a service (only one is allowed).
 */
public class DefaultGemSorter implements IGemSorter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        // NO-OP
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
}
