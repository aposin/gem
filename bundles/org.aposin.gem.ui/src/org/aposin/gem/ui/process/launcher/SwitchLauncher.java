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
package org.aposin.gem.ui.process.launcher;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.workflow.ICommand;

/**
 * Launcher wrapper to switch on/off the launch method by
 * a boolean supplier.
 * </br>
 * Some use cases for this wrapper are conditional launches
 * based on some conditions or asking the user if they are
 * sure that they would like to proceed.
 */
public final class SwitchLauncher extends LauncherWrapper {

    private final BooleanSupplier switcher;

    /**
     * Constructor.
     * 
     * @param delegate launcher to delegate all methods.
     * @param switcher supplier for switch on ({@code true})
     *                 or off ({@code false}) the launch.
     */
    public SwitchLauncher(final ILauncher delegate, final BooleanSupplier switcher) {
        super(delegate);
        this.switcher = switcher;
    }

    @Override
    public List<ICommand> launch() throws GemException {
        if (switcher.getAsBoolean()) {
            return getDelegate().launch();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<ICommand> launch(Set<IParam> params) throws GemException {
        if (switcher.getAsBoolean()) {
            return getDelegate().launch(params);
        }
        return Collections.emptyList();
    }

}
