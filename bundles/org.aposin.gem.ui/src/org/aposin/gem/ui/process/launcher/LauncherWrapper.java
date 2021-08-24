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
import java.util.Set;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.workflow.ICommand;

/**
 * Launcher implementation wrapping a launcher to add extra functionality.
 */
class LauncherWrapper implements ILauncher {

    private final ILauncher delegate;

    /**
     * Constructor.
     * 
     * @param delegate launcher to delegate all methods.
     */
    public LauncherWrapper(final ILauncher delegate) {
        this.delegate = delegate;
    }
    
    protected ILauncher getDelegate() {
        return delegate;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public INamedObject getGroup() {
        return delegate.getGroup();
    }

    @Override
    public INamedObject getLaunchScope() {
        return delegate.getLaunchScope();
    }

    @Override
    public boolean canLaunch() {
        return delegate.canLaunch();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Set<IParam> createParams() throws GemException {
        return delegate.createParams();
    }

    @Override
    public boolean requireParams() {
        return delegate.requireParams();
    }

    @Override
    public List<ICommand> launch() throws GemException {
        return delegate.launch();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public List<ICommand> launch(Set<IParam> params) throws GemException {
        return delegate.launch(params);
    }
    
    
}
