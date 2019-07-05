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
package org.aposin.gem.ui.part;

import java.util.Collection;
import java.util.List;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.part.listener.LauncherSelectionListener;
import org.aposin.gem.ui.theme.ThemeConstants;
import org.aposin.gem.ui.theme.ThemeIconRegistry;
import org.aposin.gem.ui.view.DynamicButtonGroup;
import org.aposin.gem.ui.view.DynamicButtonGroupListView;
import org.aposin.gem.ui.view.dashboard.DashboardView;
import org.aposin.gem.ui.view.dashboard.RepositoryDashboardInfoContainer;
import org.aposin.gem.ui.view.filter.EnvironmentBySessionProjectFilter;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;

/**
 * Helper class to configure some common widgets/views 
 * in different parts.
 */
public final class PartHelper {

    private PartHelper() {
        // NO-OP
    }

    private static ViewerFilter selectedProjectViewerFilter;

    public static void recreateLauncherButtons(final DynamicButtonGroupListView view,
            final Collection<ILauncher> launchers, final String themeId) {
        try {
            view.setRedraw(false);
            view.clearAllGroups();
            for (final ILauncher launcher : launchers) {
                final DynamicButtonGroup group = view.getOrCreateGroup(launcher.getGroup().getId());
                group.setText(launcher.getGroup().getDisplayName());

                final Button b = group.createButton(SWT.NONE);
                final String name = launcher.getDisplayName();
                if (name != null) {
                    b.setText(name);
                }
                final Image image =
                        ThemeIconRegistry.getInstance().getIcon(launcher).getImage(themeId);
                if (image != null && !image.isDisposed()) {
                    b.setImage(image);
                }
                addButtonLauncherListener(b, new LauncherSelectionListener(launcher));
            }
            view.layout(true);
        } finally {
            view.setRedraw(true);
        }
    }

    public static void addButtonLauncherListener(final Button b,
            final LauncherSelectionListener launcherListener) {
        b.addSelectionListener(launcherListener);
        b.setData(LauncherSelectionListener.class.getName(), launcherListener);
        setLauncherButtonEnabled(b);
    }

    public static void updateLauncherButtonsEnablement(final DynamicButtonGroupListView view) {
        view.getGroups().stream()//
                .flatMap(group -> group.getButtons().stream()) //
                .forEach(PartHelper::setLauncherButtonEnabled);
    }

    public static void setLauncherButtonEnabled(final Button b) {
        final LauncherSelectionListener launcherListener =
                (LauncherSelectionListener) b.getData(LauncherSelectionListener.class.getName());
        if (launcherListener == null) {
            b.setEnabled(false);
        } else {
            // refresh to remove cached launcher
            launcherListener.refresh();
            final ILauncher launcher = launcherListener.getLauncher();
            b.setEnabled(launcher != null && launcher.canLaunch());
        }
    }

    public static void setEnvironmentInformationData(final Session session,
            final ComboViewer projectSelectionComboViewer,
            final ComboViewer environmentSelectionComboViewer) {
        // set the data for the dropdowns (all the environments, which will be filtered afterwards)
        projectSelectionComboViewer.setInput(session.getConfiguration().getProjects());
        environmentSelectionComboViewer.setInput(session.getConfiguration().getEnvironments());
        // the environment combo will contain all the environments, but with a filter
        if (selectedProjectViewerFilter == null) {
            selectedProjectViewerFilter = new EnvironmentBySessionProjectFilter(session);
        }
    }

    /**
     * Selects the environment in both the project and the environment configured with this class.
     * 
     * @param environment
     * @param projectSelectionComboViewer
     * @param environmentSelectionComboViewer
     */
    public static void selectEnvironmentOnCombos(final IEnvironment environment,
            final ComboViewer projectSelectionComboViewer,
            final ComboViewer environmentSelectionComboViewer) {
        selectEnvironmentOnCombo(environment, environmentSelectionComboViewer);
        // should be done after the environment, to ensure that it does not enters an endless loop
        projectSelectionComboViewer.setSelection(new StructuredSelection(environment.getProject()));
    }

    /**
     * Selects the environment in a combo configured with {@link #configureEnvironmentComboViewer(Session, ComboViewer)}.
     * 
     * @param environmentSelectionComboViewer
     * @param environment
     */
    public static void selectEnvironmentOnCombo(final IEnvironment environment,
            final ComboViewer environmentSelectionComboViewer) {
        environmentSelectionComboViewer.removeFilter(selectedProjectViewerFilter);
        environmentSelectionComboViewer.setSelection(new StructuredSelection(environment));
        environmentSelectionComboViewer.setFilters(selectedProjectViewerFilter);
    }

    /**
     * Sets the dashboard content.
     * </br>
     * This method also defaults the selection to the first element,
     * so it might trigger any listener.
     * 
     * @param view dashboard view.
     * @param info content for the view.
     */
    public static void setDashboardContent(final DashboardView view,
            final List<RepositoryDashboardInfoContainer> info) {
        view.getTableViewer().setContentProvider(ArrayContentProvider.getInstance());
        view.getTableViewer().setInput(info);
        view.getTableViewer().setSelection(new StructuredSelection(info.get(0)));
    }

    /**
     * Gets the active theme.
     * 
     * @param engine theme engine.
     * @return
     */
    public static String getActiveTheme(final IThemeEngine engine) {
        final ITheme activeTheme = engine.getActiveTheme();
        return activeTheme == null ? ThemeConstants.DEFAULT_THEME_ID : activeTheme.getId();
    }
    
}
