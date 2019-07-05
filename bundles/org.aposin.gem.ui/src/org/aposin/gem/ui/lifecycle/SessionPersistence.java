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

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.service.IServiceContainer;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.Activator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for the session persistence on eclipse preferences.
 * </br>
 * Used to retrieve the latest session preferences.
 */
@Creatable
class SessionPersistence {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionPersistence.class);
    
    private static final String PREFS_NODE = Activator.PLUGIN_ID + ".session.persistence";
    private static final String PROJECT_ID = "projectId";
    private static final String ENVIRONMENT_ID = "environemntId";
    private static final String FEATURE_BRANCH_PROVIDER_ID = "featureBranchProviderId";
    private static final String FEATURE_BRANCH_ID = "featureBranchId";
    
    /**
     * Gets the persisted environment or the default one.
     * 
     * @param session current session.
     * @return non-null environment (persisted or default).
     */
    public IEnvironment getPersistedEnvironment(final Session session) {
        final IProject project = getPersistedObject(PROJECT_ID, //
                session.getConfiguration().getProjects(), //
                () -> session.getConfiguration().getProjects().get(0));

        return getPersistedObject(ENVIRONMENT_ID,
                session.getConfiguration().getEnvironments(), //
                () -> project.getEnvironments().get(0));
    }
    
    /**
     * Gets the persisted feature-branch or the default.
     * 
     * @param session current session.
     * @return feature-branch (persisted or default); {@code null} if none.
     */
    public IFeatureBranch getPersistedFeatureBranch(final Session session) {
        final IServiceContainer services = session.getConfiguration().getServiceContainer();
        final IFeatureBranchProvider provider = getPersistedObject(FEATURE_BRANCH_PROVIDER_ID, //
                List.copyOf(services.getFeatureBranchProviders()), //
                services::getDefaultFeatureBranchProvider);
        
        return getPersistedObject(FEATURE_BRANCH_ID, //
                provider.getFeatureBranches(session.getSessionEnvironment()), //
                () -> provider.getDefaultFeatureBranch(session.getSessionEnvironment()).orElse(null));
    }
    
    /**
     * Persist the session elements if required.
     * 
     * @param session to persist.
     */
    public void persistSession(final Session session) {
        boolean shouldPersist = setPersistedObject(PROJECT_ID, session.getSessionProject());
        shouldPersist |= setPersistedObject(ENVIRONMENT_ID, session.getSessionEnvironment());
        shouldPersist |= setPersistedObject(FEATURE_BRANCH_PROVIDER_ID, session.getSessionFeatureBranchProvider());
        shouldPersist |= setPersistedObject(FEATURE_BRANCH_ID, session.getSessionFeatureBranch());
        if (shouldPersist) {
            persist();
        }
    }
    
    private <T extends INamedObject> T getPersistedObject(final String identifier, //
            final List<T> possibleValues, final Supplier<T> defaultValue) {
        final String id = InstanceScope.INSTANCE.getNode(PREFS_NODE).get(identifier, null);
        if (id == null) {
            // early termination
            return defaultValue.get();
        }
        return possibleValues.stream() //
                    .filter(p -> p.getId().equals(id)) //
                    .findFirst() //
                    .orElseGet(defaultValue::get);
    }
    
    private boolean setPersistedObject(final String identifier, final INamedObject value) {
        final Preferences prefs = InstanceScope.INSTANCE.getNode(PREFS_NODE);
        if (value == null) {
            prefs.remove(identifier);
            return true;
        }
        if (!Objects.equals(prefs.get(identifier, null), value.getId())) {
            prefs.put(identifier, value.getId());
            return true;
        }
        return false;
    }
    
    private void persist() {
        try {
            InstanceScope.INSTANCE.getNode(PREFS_NODE).flush();
        } catch (final BackingStoreException e) {
            LOGGER.warn("Cannot persist session", e);
        }
    }
    
}
