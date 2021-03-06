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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DynamicButtonGroupListView extends Composite {

    private final Map<String, DynamicButtonGroup> launcherButtonsByGroupId = new HashMap<>();

    public DynamicButtonGroupListView(final Composite parent, int type) {
        super(parent, SWT.NONE);
        RowLayoutFactory.fillDefaults().type(type).applyTo(this);
    }

    public DynamicButtonGroup getOrCreateGroup(final String id) {
        return launcherButtonsByGroupId.computeIfAbsent(id, //
                newId -> new DynamicButtonGroup(this, SWT.NONE));
    }

    public Collection<DynamicButtonGroup> getGroups() {
        return launcherButtonsByGroupId.values();
    }

    public void clearAllGroups() {
        Arrays.stream(this.getChildren()).forEach(Control::dispose);
        launcherButtonsByGroupId.clear();
    }


}
