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
package org.aposin.gem.ui.part.listener.dashboard;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.ui.message.MessageRegistry;
import org.aposin.gem.ui.part.PartHelper;
import org.aposin.gem.ui.process.launcher.NullLauncher;
import org.aposin.gem.ui.view.dashboard.DashboardView;
import org.aposin.gem.ui.view.dashboard.RepositoryDashboardInfoContainer;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * Dynamic button listener creating dashboard buttons.
 */
public final class DashboardDynamicButtonListener implements ISelectionChangedListener {

    private final DashboardView view;
    private final IThemeEngine themeEngine;
    private String selectedDescriptionFormat;
    private DashboardDynamicButtonListener(final DashboardView view, final IThemeEngine themeEngine) {
        this.view = view;
        this.themeEngine = themeEngine;
    }
    
    public static DashboardDynamicButtonListener registerListener(final DashboardView view, final MessageRegistry msgRegistry, final IThemeEngine themeEngine) {
        final DashboardDynamicButtonListener listener = new DashboardDynamicButtonListener(view, themeEngine);
        msgRegistry.register(listener::setDescriptionFormat, msg -> msg.dashboardView_labelFormat_selectionDescription);
        view.getTableViewer().addSelectionChangedListener(listener);
        // trigger when registered
        listener.trigger();
        listener.recreateLauncherButtons(view.getTableViewer().getStructuredSelection());
        return listener;
    }
    
    public void setDescriptionFormat(final String format) {
        this.selectedDescriptionFormat = format;
    }
    
    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        recreateLauncherButtons(event.getStructuredSelection());
    }
    
    /**
     * Trigger the listener as if it was re-selected.
     * </br>
     * This is useful, for example, to trigger a theme change.
     */
    public void trigger() {
        this.recreateLauncherButtons(view.getTableViewer().getStructuredSelection());
    }
    
    private void recreateLauncherButtons(final IStructuredSelection selection) {
        final RepositoryDashboardInfoContainer dashboardInfo = (RepositoryDashboardInfoContainer) selection.getFirstElement();
        final String description;
        final List<ILauncher> launchers = new ArrayList<>();
        if (dashboardInfo == null ) {
            description = MessageFormat.format(selectedDescriptionFormat, "none");
        } else {
            description = MessageFormat.format(selectedDescriptionFormat, dashboardInfo.getRepositoryName());
            launchers.addAll(dashboardInfo.getRepositoryLaunchers());
        }
        
        if (launchers.isEmpty()) {
            launchers.add(new NullLauncher("None"));
        }
        view.getDynamicButtonsLabel().setText(description);
        PartHelper.recreateLauncherButtons(view.getDynamicButtons(), launchers, PartHelper.getActiveTheme(themeEngine));
    }

}
