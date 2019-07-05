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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.BundleProperties;
import org.aposin.gem.ui.lifecycle.event.RefreshedObjectEvent;
import org.aposin.gem.ui.lifecycle.event.SessionEnvironmentChangeEvent;
import org.aposin.gem.ui.lifecycle.event.SessionFeatureBranchChangeEvent;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;

/**
 * Contains session data for the application.
 */
public final class Session {

    @Inject
    @Translation
    public static BundleProperties bundleProperties;

    @Inject
    @Translation
    public static Messages messages;

    @Inject
    private static SessionPersistence sessionPersistence;
    
    @Inject
    private IConfiguration config;
    @Inject
    private IEventBroker eventBroker;
    @Inject
    private SessionInitializer initializer;

    private IEnvironment sessionEnvironment;
    private IFeatureBranch sessionFeatureBranch;
    
    /**
     * Reset the session.
     */
    public void refresh(final IProgressMonitor monitor) {
        // set the initializer
        initializer.setProgressMonitor(monitor);
        monitor.subTask("Refreshing configuration");
        config.refresh();
        monitor.worked(1);
        eventBroker.send(RefreshedObjectEvent.SESSION_CONFIG_REFRESH_TOPIC,
                new RefreshedObjectEvent<>(config));
        monitor.subTask("Refreshing session");
        postRefresh();
        monitor.worked(1);
        initializer.cleanProgressMonitor();
    }

    @PostConstruct
    private void postRefresh() {
        resetEnvironment();
    }
    
    protected void resetEnvironment() {
        // always reset if the environment is null
        boolean resetEnvironment = true;
        if (sessionEnvironment != null) {
            resetEnvironment = config.getEnvironments().stream() //
                    .noneMatch(sessionEnvironment::equals);
        }
        if (resetEnvironment) {
            setSessionEnvironment(sessionPersistence.getPersistedEnvironment(this));
        }
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public IEventBroker getEventBroker() {
        return eventBroker;
    }

    public IEnvironment getSessionEnvironment() {
        return sessionEnvironment;
    }

    public IFeatureBranch getSessionFeatureBranch() {
        return sessionFeatureBranch;
    }

    public IProject getSessionProject() {
        return sessionEnvironment.getProject();
    }

    public IFeatureBranchProvider getSessionFeatureBranchProvider() {
        if (sessionFeatureBranch == null) {
            return config.getServiceContainer().getDefaultFeatureBranchProvider();
        }
        return sessionFeatureBranch.getProvider();
    }

    /**
     * Sets the session environment. </br>
     * Sends the following events, in order:
     * <ol>
     * <li>{@link SessionEnvironmentChangeEvent#TOPIC} with the previous environment and the new
     * one</li>
     * <li>{@link SessionFeatureBranchChangeEvent#TOPIC} invalidating the feature branch</li>
     * </ol>
     * 
     * @param newEnvironment
     */
    public void setSessionEnvironment(final IEnvironment newEnvironment) {
        final IEnvironment oldEnvironment = sessionEnvironment;
        sessionEnvironment = newEnvironment;
        eventBroker.post(SessionEnvironmentChangeEvent.TOPIC,
                new SessionEnvironmentChangeEvent(oldEnvironment, newEnvironment));
        if (sessionFeatureBranch == null) {
            setSessionFeatureBranch(sessionPersistence.getPersistedFeatureBranch(this));
        } else if (!sessionFeatureBranch.getEnvironment().equals(newEnvironment)) {
            final IFeatureBranchProvider provider = getSessionFeatureBranchProvider();
            final IFeatureBranch newFeatureBranch = provider
                    // get the matching feature branch
                    .getMatchingFeatureBranch(newEnvironment, sessionFeatureBranch)
                    // try the default (if any)
                    .or(() -> provider.getDefaultFeatureBranch(newEnvironment))
                    // get the first or set to null
                    .orElse(null);
            setSessionFeatureBranch(newFeatureBranch);
        }
        sessionPersistence.persistSession(this);
    }

    public void setSessionFeatureBranch(final IFeatureBranch newFeatureBranch) {
        if (newFeatureBranch != null
                && !newFeatureBranch.getEnvironment().equals(sessionEnvironment)) {
            throw new IllegalArgumentException(
                    "Feature-branch environment does not match the session environment");
        }
        final IFeatureBranch oldFeatureBranch = sessionFeatureBranch;
        sessionFeatureBranch = newFeatureBranch;
        eventBroker.post(SessionFeatureBranchChangeEvent.TOPIC,
                new SessionFeatureBranchChangeEvent(oldFeatureBranch, newFeatureBranch));
        sessionPersistence.persistSession(this);
    }
}
