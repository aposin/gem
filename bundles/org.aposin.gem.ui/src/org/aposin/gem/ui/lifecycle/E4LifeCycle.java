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
package org.aposin.gem.ui.lifecycle;

import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import org.aposin.gem.core.GemException;
import org.aposin.gem.logging.e4.SLF4JLogger;
import org.aposin.gem.logging.e4.SLF4JLoggerProvider;
import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.BundleProperties;
import org.aposin.gem.ui.handler.QuitHandler;
import org.aposin.gem.ui.theme.ThemeConstants;
import org.aposin.gem.ui.theme.icons.IGemIcon;
import org.aposin.gem.ui.theme.icons.GemSvgIcon;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.osgi.service.event.Event;
import org.slf4j.LoggerFactory;

/**
 * This is a stub implementation containing e4 LifeCycle annotated methods.<br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the
 * <em>org.eclipse.core.runtime.products' extension point</em>) that references this class.
 **/
public final class E4LifeCycle {

    @Inject
    @Translation
    public static BundleProperties bundleProperties;

    @PostContextCreate
    void postContextCreate(final IEclipseContext workbenchContext) {
        replaceLogger(workbenchContext);
        setJFaceWindowDefaults();
        initializeSession(workbenchContext);
    }

    private static void replaceLogger(final IEclipseContext workbenchContext) {
        // hook the logger for the e4 workbench
        workbenchContext.set(Logger.class,
                new SLF4JLogger(LoggerFactory.getLogger("org.eclipse.e4.ui.workbench")));
        workbenchContext.set(ILoggerProvider.class, new SLF4JLoggerProvider());
    }

    private static void setJFaceWindowDefaults() {
        // sets the default exception handler to show the error as a dialog
        Window.setExceptionHandler(e -> handleException(e.getLocalizedMessage(), e));
        // set the default window image for all dialogs
        // TODO - should this also check/put in the registry to be able to reuse/substitute by an
        // extension?
        final IGemIcon icon =
                new GemSvgIcon("window_gem_icon", Activator.getResource("icons/gem/gem.svg"));
        // TODO - this does not work for any reason with the ProgressMonitorDialog
        Window.setDefaultImage(icon.getImage(ThemeConstants.DEFAULT_THEME_ID));
    }

    private static void handleException(final String msg, final Throwable e) {
        final String errorTitle;
        if (e instanceof GemException) {
            errorTitle = "GEM error";
        } else {
            errorTitle = "Unexpected error";
        }
        // get the multi status with the stacktrace elements
        final IStatus status;
        if (e.getCause() == null) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage());
        } else {
            final Throwable toDetails = e.getCause();
            toDetails.fillInStackTrace();
            status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, toDetails.getLocalizedMessage(), e);
            for (final StackTraceElement stackTrace: toDetails.getStackTrace()) {
                ((MultiStatus) status).add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, stackTrace.toString()));
            }
        }
        
        // open error dialog
        ErrorDialog.openError(null, errorTitle, msg, status);
    }

    /**
     * Initializes the configuration for the GEM core.
     * 
     * @param workbenchContext context.
     */
    private static void initializeSession(final IEclipseContext workbenchContext) {
        try {
            final SessionInitializer initializer =
                    ContextInjectionFactory.make(SessionInitializer.class, workbenchContext);
            new ProgressMonitorDialog(null).run(true, false, initializer);
        } catch (final InvocationTargetException | InterruptedException e) {
            final GemException toThrow;
            if (e.getCause() instanceof GemException) {
                toThrow = (GemException) e.getCause();
            } else {
                toThrow = new GemException("Error on session initialization", e.getCause());
            }
            handleException(toThrow.getMessage(), toThrow.getCause() == null ? toThrow : toThrow.getCause());
            // rethrow to stop application
            throw toThrow;
        }
    }

    @PreSave
    void preSave(IEclipseContext workbenchContext) {
        // no pre-save hook yet
    }

    @ProcessAdditions
    void processAdditions(IEclipseContext workbenchContext) {
        // no additions hook yet
    }

    @ProcessRemovals
    void processRemovals(IEclipseContext workbenchContext) {
        // no removals hook yet
    }

    /**
     * Execute startup tasks.
     * 
     * @param event event for the app startup completed
     * @param window the active window (Main Window)
     */
    @Optional
    @Inject
    private void appStartupComplete(@UIEventTopic(UILifeCycle.APP_STARTUP_COMPLETE) Event event,
            @Active final MWindow window) {
        Activator.LOGGER.trace("appStartupComplete");
        // first, change the label of the main window to add the version
        window.setLabel(bundleProperties.application_name);
        // add a close handler to the window to open the confirm dialog as on the QuitHandler
        window.getContext().set(IWindowCloseHandler.class, w -> QuitHandler.confirmClose(null));
    }

}
