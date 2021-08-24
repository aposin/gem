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
package org.aposin.gem.core.api.service.launcher;

import java.util.List;
import java.util.Map;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.IGemService;

/**
 * Service for providing launchers at the environment level.
 */
public interface IEnvironmentLauncherProvider extends IGemService {

    /**
     * Gets the launchers for the environment.
     * 
     * @param environment 
     * @return launchers for the environment itself; empty if none.
     */
    public List<ILauncher> getLaunchers(final IEnvironment environment);

    /**
     * Gets the per-repository launchers for the environment.
     * </br>
     * This returns a map of repository and launcher that are related with the environment.
     * 
     * @param environment
     * @return launchers for the repositories on the environment; empty if none.
     */
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IEnvironment environment);
    
}
