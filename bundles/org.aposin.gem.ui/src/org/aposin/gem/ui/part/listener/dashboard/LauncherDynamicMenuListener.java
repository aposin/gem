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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.ui.part.listener.LauncherSelectionListener;
import org.aposin.gem.ui.theme.ThemeConstants;
import org.aposin.gem.ui.theme.ThemeIconRegistry;
import org.aposin.gem.ui.view.dashboard.RepositoryDashboardInfoContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which creates the menu dinamically for a table based on a launcher supplier.
 */
public class LauncherDynamicMenuListener implements Listener {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final Table table;
    
    public static final Listener registerListener(final Table table) {
        final Listener listener = new LauncherDynamicMenuListener(table);
        table.addListener(SWT.MouseDown, listener);
        return listener;
    }
    
    private LauncherDynamicMenuListener(final Table table) {
        this.table = table;
    }
    
    @Override
    public void handleEvent(final Event event) {
        if (event.type == SWT.MouseDown) {
            logger.trace("MouseDown");
            final Point eventPoint = new Point(event.x, event.y);
            final TableItem item = table.getItem(eventPoint);
            if (item == null) {
                table.setMenu(null);
                logger.trace("Remove context-menu for no item");
            } else {
                logger.trace("Create context-menu for item: {}", item);
                final RepositoryDashboardInfoContainer dashboardInfo = (RepositoryDashboardInfoContainer) item.getData();
                final List<ILauncher> launchers = dashboardInfo.getRepositoryLaunchers();
                final Menu menu = createMenuByGroup(table, launchers);
                table.setMenu(menu);
            }
        }
    }
    
    private static Menu createMenuByGroup(final Table table, final List<ILauncher> launchers) {
        final Menu menu = new Menu(table);
        // using LinkedHashMap to keep order
        final Set<INamedObject> groups = new HashSet<>();
        final Map<INamedObject, List<ILauncher>> launchersByGroup = launchers.stream()//
                .peek(l -> groups.add(l.getGroup())) //
                .collect(Collectors.groupingBy(ILauncher::getGroup, LinkedHashMap::new, Collectors.toList()));
        // organize by group in any case
        for (final Map.Entry<INamedObject, List<ILauncher>> entry: launchersByGroup.entrySet()) {
                groups.remove(entry.getKey());
                for (final ILauncher launcher: entry.getValue()) {
                    final MenuItem item = createMenuItem(menu, SWT.PUSH, launcher);
                    // append the group displayname in case of collapsed names
                    item.setText(entry.getKey().getDisplayName() +  " " + launcher.getDisplayName());
                    addMenuItemLauncherListener(item, new LauncherSelectionListener(launcher));
                }
                if (!groups.isEmpty()) {
                    // add a separator between groups
                    createMenuItem(menu, SWT.SEPARATOR, entry.getKey());
                }
        }
        
        return menu;
    }
    
    private static MenuItem createMenuItem(final Menu menu, final int style, final INamedObject namedObject) {
        final MenuItem menuItem = new MenuItem(menu, style);
        final String name = namedObject.getDisplayName();
        if (name != null && !name.isEmpty()) {
            menuItem.setText(name);
        }
        // menu cannot be themed, so using default
        final Image image = ThemeIconRegistry.getInstance().getIcon(namedObject).getImage(ThemeConstants.DEFAULT_THEME_ID);
        if (image != null && !image.isDisposed()) {
            menuItem.setImage(image);
        }
        return menuItem;
    }
    
    private static void addMenuItemLauncherListener(final MenuItem menuItem,
            final LauncherSelectionListener launcherListener) {
        menuItem.addSelectionListener(launcherListener);
        menuItem.setData(LauncherSelectionListener.class.getName(), launcherListener);
        setLauncherMenuItemEnabled(menuItem);
    }
    
    private static void setLauncherMenuItemEnabled(final MenuItem menuItem) {
        final LauncherSelectionListener launcherListener =
                (LauncherSelectionListener) menuItem.getData(LauncherSelectionListener.class.getName());
        if (launcherListener == null) {
            menuItem.setEnabled(false);
        } else {
            // refresh to remove cached launcher
            launcherListener.refresh();
            final ILauncher launcher = launcherListener.getLauncher();
            menuItem.setEnabled(launcher != null && launcher.canLaunch());
        }
    }
    
}
