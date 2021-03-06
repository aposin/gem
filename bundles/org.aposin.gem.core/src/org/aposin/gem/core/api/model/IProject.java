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
package org.aposin.gem.core.api.model;

import java.util.List;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.IRefreshable;

/**
 * Definition of a project.
 */
public interface IProject extends INamedObject, IRefreshable {

    /**
     * Gets the name of the project.
     * 
     * @return name name of the project.
     */
    @Override
    public String getName();

    /**
     * Gets the display name of the project.
     * 
     * @return display name.
     */
    @Override
    public String getDisplayName();

    /**
     * Gets the set of environments defined for this project.
     * 
     * @return environments defined for this project.
     */
    public List<IEnvironment> getEnvironments();

    /**
     * Gets the branch prefix that identifies branches attached to this project.
     * </br>
     * Default implementation returns {@link #getName()}.
     * 
     * @return branch prefix for the project.
     * @see IEnvironment#getBranchPrefix()
     */
    public default String getBranchPrefix() {
        return getName();
    }

    /**
     * Get the obsolete environments for this project.
     * </br>
     * Obsolete environments are identified in an implementation-specific
     * manner and some functionalities might not work for the GEM workflow.
     * 
     * @return obsolete environments.
     */
    public List<IEnvironment> getObsoleteEnvironments();

}
