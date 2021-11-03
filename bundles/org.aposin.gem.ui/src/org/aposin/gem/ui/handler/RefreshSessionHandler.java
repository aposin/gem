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


import java.lang.reflect.InvocationTargetException;

import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Handler to refresh the session.
 */
public class RefreshSessionHandler extends GemAbstractSessionHandler {

    @Override
    public void doExecute(final Session session) throws Exception {
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        // possible exceptions are handled by the super class
        dialog.run(true, false, new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask(Session.messages.refreshSessionHandler_message_progressMonitorStart,
                        IProgressMonitor.UNKNOWN);
                session.refresh(monitor);
            }
        });
    }
}
