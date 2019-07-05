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
package org.aposin.gem.ui.view;

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.ui.view.labelprovider.NamedObjectLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Helper class to create some common widgets for the views.
 */
class ViewHelper {

    private ViewHelper() {
        // NO-OP
    }

    public static final ComboViewer createProjectCombo(final Composite composite) {
        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        final ComboViewer viewer = new ComboViewer(combo);
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new NamedObjectLabelProvider(IProject.class));
        return viewer;
    }

    public static final ComboViewer createEnvironmentCombo(final Composite composite) {
        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        final ComboViewer viewer = new ComboViewer(combo);
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new NamedObjectLabelProvider(IEnvironment.class));
        return viewer;
    }

}
