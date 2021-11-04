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
