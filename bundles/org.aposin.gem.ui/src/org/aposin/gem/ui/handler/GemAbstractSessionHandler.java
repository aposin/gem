package org.aposin.gem.ui.handler;

import javax.inject.Inject;

import org.aposin.gem.ui.lifecycle.GemExceptionManager;
import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.e4.core.di.annotations.Execute;

/**
 * Helper class to implement GEM handlers for the e4 application.
 * </br>
 * Extensions of this class can be used as handlers and they should not
 * provide a method with the {@link Execute} annotation (already implemented
 * with {@link #execute(Session)}.
 */
public abstract class GemAbstractSessionHandler {

    @Inject
    private GemExceptionManager handler;

    /**
     * Executes the handler and manage all possible exceptions.
     * 
     * @param session the active session (injected).
     */
    @Execute
    public final void execute(final Session session) {
        try {
            doExecute(session);
        } catch (Exception e) {
            handler.handleException(e);
        }
    }

    /**
     * Executes the handler.
     * </br>
     * Exceptions thrown by this method are handled by the
     * ´{@link GemExceptionManager} provided by the framework.
     * 
     * @param session the active session.
     * @throws Exception if any expected or unexpected error happes.
     */
    public abstract void doExecute(final Session session) throws Exception;

}
