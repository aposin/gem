/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur FÃ¶rderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.ui.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.ConfigurationLoader;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.config.IPreferences;
import org.aposin.gem.core.api.config.provider.IConfigFileProvider;
import org.aposin.gem.core.api.config.provider.git.GitConfigFileProvider;
import org.aposin.gem.core.api.config.provider.git.GitConfigProviderHook;
import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Runnable to initialize the session and set to the context.
 */
@Creatable
public class SessionInitializer extends GitConfigProviderHook implements IRunnableWithProgress, IConfigFileProvider {

    private static final Path CONFIG_FOLDER = Activator.APP_USER_PATH.resolve("configuration");

    /**
     * File where the user preferences are stored.
     */
    public static final Path PREFS_FILE = Activator.APP_USER_PATH.resolve("user_prefs.properties");

    //TODO make file and folder configurable
    private final IConfigFileProvider configFileProvider = new GitConfigFileProvider(PREFS_FILE, CONFIG_FOLDER, this);

    @Inject
    @Translation
    private static Messages messages;

    @Inject
    private IEclipseContext workbenchContext;

    @Inject
    private UISynchronize uiSynchronize;

    private IProgressMonitor monitor = null;

    @Override
    public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        setProgressMonitor(monitor);
        doRun();
        cleanProgressMonitor();
    }

    public void setProgressMonitor(final IProgressMonitor monitor) {
        if (this.monitor != null) {
            throw new GemException("Already running");
        }
        this.monitor = monitor;
    }

    public void cleanProgressMonitor() {
        this.monitor.done();
        this.monitor = null;
    }

    private void doRun() {
        monitor.beginTask(messages.sessionInitializer_message_progressMonitorStart, IProgressMonitor.UNKNOWN);
        final IConfiguration config = new ConfigurationLoader().withConfigFileProvider(this).load();
        workbenchContext.set(IConfiguration.class, config);

        monitor.subTask(messages.sessionInitializer_message_progressMonitorLoadSession);
        final Session session = ContextInjectionFactory.make(Session.class, workbenchContext);
        // add to the context the sesion and the initializer
        workbenchContext.set(Session.class, session);
    }

    @Override
    public Path getPrefFile() {
        if (monitor != null) {
            monitor.subTask(messages.sessionInitializer_message_progressMonitorLoadPrefs);
        }
        return configFileProvider.getPrefFile();
    }

    @Override
    public Path getConfigFile(final IPreferences prefs) {
        if (monitor != null) {
            monitor.subTask(messages.sessionInitializer_message_progressMonitorLoadConfig);
        }
        return configFileProvider.getConfigFile(prefs);
    }

    @Override
    public Path getRelativeToConfigFile(final String relativePath) throws GemException {
        return configFileProvider.getRelativeToConfigFile(relativePath);
    }
    
    @Override
    public boolean checkoutWhenDifferentBranch(final String configBranch, final String currentBranch) {
        final AtomicBoolean checkout = new AtomicBoolean(false);
        uiSynchronize.syncExec(() -> 
            checkout.set(MessageDialog.openQuestion(null, //
                    messages.sessionInitializer_title_openQuestionDialog, //
                    MessageFormat.format(
                            messages.sessionInitializer_messageFormat_checkoutDifferentBranchQuestionDialog,
                            currentBranch, configBranch)))
        );
        
        return checkout.get();
    }
    
    @Override
    public boolean proceedIfPullFails(final String configBranch) {
        final AtomicBoolean proceed = new AtomicBoolean(false);
        uiSynchronize.syncExec(() -> 
            proceed.set(MessageDialog.openQuestion(null, //
                    messages.sessionInitializer_title_openQuestionDialog, //
                MessageFormat.format(//
                            messages.sessionInitializer_messageFormat_pullFailsProceedQuestionDialog,
                        configBranch)
            ))
        );
        return proceed.get();
    }

}
