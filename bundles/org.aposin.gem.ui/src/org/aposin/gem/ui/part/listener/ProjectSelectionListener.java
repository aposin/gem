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
package org.aposin.gem.ui.part.listener;

import java.util.Optional;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.part.PartHelper;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.events.SelectionListener;

/**
 * {@link SelectionListener} when a new {@link IProject} has been set.
 */
public final class ProjectSelectionListener
        extends TypedSingleSelectionChangedListener<IProject> {

    private final Session session;
    private final ComboViewer environmentSelectionComboViewer;

    public ProjectSelectionListener(final Session session,
            final ComboViewer environmentSelectionComboViewer) {
        this.session = session;
        this.environmentSelectionComboViewer = environmentSelectionComboViewer;
    }

    @Override
    public void selectionChanged(IProject project) {
        // only update if the project is not equal
        if (!session.getSessionProject().equals(project)) {
            final IEnvironment environment = getEnvironmentToSelect(project);
            PartHelper.selectEnvironmentOnCombo(environment, environmentSelectionComboViewer);
        }
    }

    private IEnvironment getEnvironmentToSelect(final IProject project) {
        final String sessionEnvironmentId = session.getSessionEnvironment().getId();
        final Optional<IEnvironment> maybeMatching = project.getEnvironments().stream()//
                .filter(env -> env.getId().equals(sessionEnvironmentId))//
                .findFirst();
        return maybeMatching.orElseGet(() -> project.getEnvironments().get(0));
    }
}
