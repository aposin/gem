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

import java.util.Comparator;

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;

/**
 * Service to provide a sorting algorithm for several GEM objects.
 * </br>
 * IMPORTANT: should be provided only once.
 */
public interface IGemSorter extends IGemService {

    /**
     * {@inheritDoc}
     */
    @Override
    public default String getName() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public default String getDisplayName() {
        return getId();
    }

    /**
     * Get the comparator for the projects.
     * 
     * @return project comparator.
     */
    public Comparator<IProject> getProjectComparator();

    /**
     * Get the comparator for the environments.
     * 
     * @return environment comparator.
     */
    public Comparator<IEnvironment> getEnvironmentComparator();

    public Comparator<IFeatureBranchProvider> getFeatureBranchProviderComparator();

}
