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
package org.aposin.gem.ui.part.listener.workflow;

import java.util.function.Function;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.workflow.WorkflowException;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.part.listener.LauncherSelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract selection listener which gets a launcher depending on
 * some conditions and workflow finished hook.
 */
public abstract class SessionWorkflowLauncherListener<T> extends LauncherSelectionListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Session session;
    private final Function<Session, T> sessionObjectProvider;
    private final Function<T, ILauncher> workflowFunction;

    private T currentSessionObject;

    /**
     * Default constructor.
     * 
     * @param sesion
     * @param workflowFunction
     */
    protected SessionWorkflowLauncherListener(final Session session,
            final Function<Session, T> sessionObjectProvider,
            final Function<T, ILauncher> workflowFunction) {
        super(null);
        this.session = session;
        this.sessionObjectProvider = sessionObjectProvider;
        this.workflowFunction = workflowFunction;

    }

    /**
     * Gets the session for the workflow launcher.
     * 
     * @return session.
     */
    protected final Session getSession() {
        return session;
    }

    @Override
    public final ILauncher getLauncher() {
        if (launcher == null) {
            try {
                launcher = getWorkflowLauncher();
            } catch (final WorkflowException e) {
            	logger.warn("Ignored exception getting workflow launcher", e);
            }
        }
        return launcher;
    }
    
    /**
     * Gets the current session object used to get the launcher.
     */
    public final T getCurrentSessionObject() {
    	return currentSessionObject;
    }
    
    /**
     * Gets the workflow launcher.
     * 
     * @return launcher.
     * @throws WorkflowException if there is any problem.
     */
    private final ILauncher getWorkflowLauncher() throws WorkflowException {
        if (currentSessionObject == null) {
        	currentSessionObject = sessionObjectProvider.apply(session);
        }
        return currentSessionObject == null ? null : workflowFunction.apply(currentSessionObject);
    }


    /**
     * Hook when the workflow have finished.
     */
    public abstract void onWorkflowFinished();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void widgetSelected(final SelectionEvent event) {
        try {
            super.widgetSelected(event);
        } finally {
            onWorkflowFinished();
            refresh();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void refresh() {
    	currentSessionObject = null;
        launcher = null;
        super.refresh();
    }

}
