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
package org.aposin.gem.ui.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DynamicButtonGroupListView extends ScrolledComposite {

    private final Map<String, DynamicButtonGroup> launcherButtonsByGroupId = new HashMap<>();

    public enum TYPE {

        HORIZONTAL(SWT.HORIZONTAL), //
        VERTICAL(SWT.VERTICAL);

        private final int rowLayout;

        private TYPE(final int rowLayout) {
            this.rowLayout = rowLayout;
        }
    }

    private Composite container;

    public DynamicButtonGroupListView(final Composite parent, TYPE type) {
        // TODO: change to the required oone for the type
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        setLayout(new FillLayout());
        container = new Composite(parent, SWT.NONE);
        container.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
        this.setContent(container);
        RowLayoutFactory.fillDefaults().type(type.rowLayout).applyTo(container);
    }

    public DynamicButtonGroup getOrCreateGroup(final String id) {
        return launcherButtonsByGroupId.computeIfAbsent(id, //
                newId ->
                {
                    return new DynamicButtonGroup(container, SWT.NONE);
                });
    }

    public Collection<DynamicButtonGroup> getGroups() {
        return launcherButtonsByGroupId.values();
    }

    public void clearAllGroups() {
        Arrays.stream(this.getChildren()).forEach(Control::dispose);
        launcherButtonsByGroupId.clear();
    }


}
