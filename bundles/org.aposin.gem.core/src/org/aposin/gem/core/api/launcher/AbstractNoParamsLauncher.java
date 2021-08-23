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
import org.aposin.gem.core.Activator;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.workflow.ICommand;

/**
 * Helper class to implement a launcher without parameters.
 * </br>
 * All methods related to {@link IParam} are final to prevent inconsistent
 * implementations.
 */
public abstract class AbstractNoParamsLauncher implements ILauncher {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public final Set<IParam> createParams() throws GemException {
        throw new GemException("Not params are required for " + getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean requireParams() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<ICommand> launch(@SuppressWarnings("rawtypes") final Set<IParam> params)
            throws GemException {
        Activator.LOGGER.warn("{} launcher does not require parameters: ignoring params",
                getName());
        Activator.LOGGER.warn("Ignored params: {}", params);
        return launch();
    }

}
