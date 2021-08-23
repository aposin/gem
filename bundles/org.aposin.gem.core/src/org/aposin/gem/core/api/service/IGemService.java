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

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;

/**
 * Service to retrieve from {@link IConfiguration}.
 */
public interface IGemService extends INamedObject {

    /**
     * Gets the ID for the {@link IGemService}.
     * </br>
     * Defaults to the class name of the provider.
     */
    @Override
    public default String getId() {
        return this.getClass().getName();
    }

    /**
     * Gets the name for the service.
     * 
     * @return unique name for the service.
     */
    @Override
    public String getName();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName();

    /**
     * Sets the configuration for the {@link IGemService}.
     * </br>
     * This allows to configure it on demand.
     * 
     * @param config configuration
     * @throws GemConfigurationException if there is a problem configuring the service.
     */
    public void setConfig(final IConfiguration config) throws GemConfigurationException;
}
