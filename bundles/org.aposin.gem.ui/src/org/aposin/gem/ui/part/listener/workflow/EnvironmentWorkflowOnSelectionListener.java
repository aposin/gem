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
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.lifecycle.event.EnvironmentSynchronizedEvent;

/**
 * Selection listener which gets a launcher from the session {@link IEnvironment}.
 */
public class EnvironmentWorkflowOnSelectionListener
        extends SessionWorkflowLauncherListener<IEnvironment> {

    /**
     * Default constructor.
     * 
     * @param sesion
     * @param workflowFunction
     */
    public EnvironmentWorkflowOnSelectionListener(final Session session,
            final Function<IEnvironment, ILauncher> workflowFunction) {
        super(session, Session::getSessionEnvironment, workflowFunction);
    }

    /**
     * {@inheritDoc}.
     * </br>
     * Sends the {@link EnvironmentSynchronizedEvent#TOPIC}.
     */
    @Override
    public void onWorkflowFinished() {
        if (getCurrentSessionObject() != null) {
            getSession().getEventBroker().send(EnvironmentSynchronizedEvent.TOPIC,
                    new EnvironmentSynchronizedEvent(getCurrentSessionObject()));
        }
    }
}
