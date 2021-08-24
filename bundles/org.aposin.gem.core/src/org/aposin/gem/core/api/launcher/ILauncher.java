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
package org.aposin.gem.core.api.launcher;

import java.util.List;
import java.util.Set;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.workflow.ICommand;

/**
 * Interface representing a launcher.
 */
public interface ILauncher extends INamedObject {

    /**
     * {@inheritDoc}
     * </br>
     * Default implementation returns the {@link #getName()}
     * joined with <code>_</code> to the {@link #getLaunchScope()}
     * ID.
     */
    @Override
    default String getId() {
        return getName() + '_' + getLaunchScope().getId();
    }

    /**
     * Gets the name of the launcher.
     * </br>
     * Same launcher for different scopes <em>must</em>
     * have the same name to be identified.
     */
    @Override
    public String getName();

    /**
     * Gets the group for the launchers.
     * 
     * @return group for the launchers.
     */
    public INamedObject getGroup();

    /**
     * Gets the launch scope.
     * </br>
     * It might not be the same as the type.
     * 
     * @return the launch scope.
     */
    public INamedObject getLaunchScope();

    /**
     * Checks if {@link #launch()} can be called.
     * 
     * @return {@code true} if it can be launched; {@code false}
     */
    public boolean canLaunch();

    /**
     * Create a new set of parameters for the launcher.
     * 
     * @return create the initial set of bindings.
     * @throws GemException if parameters are not required.
     */
    @SuppressWarnings("rawtypes")
    public Set<IParam> createParams() throws GemException;

    /**
     * Checks if the bindings are required for launching.
     * 
     * @return {@code true} if launching require parameters (even if they are optional);
     *         {@code false otherwise}.
     */
    public boolean requireParams();

    /**
     * Launch for the scope.
     * 
     * @return list of commands to run in parallel and track the progress;
     *         empty list if the launcher is it is not required.
     * @throws GemException if it cannot be launched.
     */
    public List<ICommand> launch() throws GemException;

    /**
     * Launch for the scope with provided bindings.
     * 
     * @param params set of parameters filled in.
     * @return list of commands to run in parallel and track the progress;
     *         empty list if the launcher is it is not required.
     * @throws GemException if it cannot be launched.
     */
    public List<ICommand> launch(@SuppressWarnings("rawtypes") final Set<IParam> params)
            throws GemException;

}
