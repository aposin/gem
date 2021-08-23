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

import java.util.List;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.workflow.ICommand;

/**
 * Launcher for mis-configured stuff.
 */
public final class NullLauncher extends AbstractNoParamsLauncher {

    /**
     * Launcher name,
     */
    public static final String NAME = "null";
    
    public final String displayName;
    
    public NullLauncher(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Overriden to avoid NPE.
     */
    @Override
    public String getId() {
        return NAME;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public INamedObject getGroup() {
        return new INamedObject() {

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getDisplayName() {
                return "";
            }
        };
    }

    @Override
    public INamedObject getLaunchScope() {
        return null;
    }

    @Override
    public boolean canLaunch() {
        return false;
    }

    @Override
    public List<ICommand> launch() throws GemException {
        // should never launch anyway
        return null;
    }
    
}
