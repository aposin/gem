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
package org.aposin.gem.ui.part.listener.workflow;

import java.util.function.Function;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.lifecycle.event.EnvironmentSynchronizedEvent;

/**
 * Selection listener which gets a launcher from the session {@link IFeatureBranch}.
 */
public final class FeatureBranchWorkflowOnSelectionListener
        extends SessionWorkflowLauncherListener<IFeatureBranch> {

    /**
     * Default constructor.
     * 
     * @param sesion
     * @param workflowFunction
     */
    public FeatureBranchWorkflowOnSelectionListener(final Session session,
            final Function<IFeatureBranch, ILauncher> workflowFunction) {
        super(session, Session::getSessionFeatureBranch, workflowFunction);
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
                    new EnvironmentSynchronizedEvent(getCurrentSessionObject().getEnvironment()));
        }
    }
}
