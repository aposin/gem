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

import java.util.List;

import org.aposin.gem.core.api.config.IConfiguration;

/**
 * Service that creates services on demand from the configuration.
 * 
 * @param <S> service class (should never be another {@link IGemServiceCreator}).
 */
public interface IGemServiceCreator<S extends IGemService> extends IGemService {

    /**
     * Gets the type of service that will be created.
     * 
     * @return type of the service.
     */
    public Class<S> getType();

    /**
     * Creates the services.
     * </br>
     * Note that the configuration will be set on the creator before calling {@link #createServices()}
     * and after creation the list of services will set again the configuration with
     * {@link IGemService#setConfig(IConfiguration)}, so here it can be used only
     * to determine which services should be created by a plug-in.
     * 
     * @param configuration the configuration
     * 
     * @return list of created services.
     */
    public List<S> createServices();

}
