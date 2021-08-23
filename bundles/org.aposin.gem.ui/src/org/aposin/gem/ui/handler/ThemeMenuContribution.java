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
package org.aposin.gem.ui.handler;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

/**
 * Menu contribution implementation which provides menu entries for each available CSS theme.
 * Selecting a dynamically created menu item switches the theme to the selected entry.
 */
public class ThemeMenuContribution {

    @Inject
    private IThemeEngine themeEngine;

    /**
     * Creates the menu items to swich the CSS theme.
     * 
     * @param items the list where to add the menu items
     */
    @AboutToShow
    public void createThemeMenuEntries(List<MMenuElement> items) {
        for (final ITheme theme : themeEngine.getThemes()) {
            final String menuItemId = theme.getId();
            final MDirectMenuItem menuItem = MMenuFactory.INSTANCE.createDirectMenuItem();
            menuItem.setElementId(menuItemId);
            menuItem.setLabel(theme.getLabel());
            menuItem.setContributorURI("platform:/plugin/org.aposin.gem.ui");
            menuItem.setContributionURI(
                    "bundleclass://org.aposin.gem.ui/org.aposin.gem.ui.handler.ThemeMenuContribution");
            if (themeEngine.getActiveTheme() == theme) {
                // menuItem.setSelected(true); //TODO not supported by e4, maybe us some kind of
                // icon
            }
            items.add(menuItem);
        }
    }

    /**
     * Changes the currently active CSS theme bound to the given menu item.
     * 
     * @param item the selected menu item
     */
    @Execute
    public void setTheme(MDirectMenuItem item) {
        final String themeId = item.getElementId();
        themeEngine.setTheme(themeId, true);
    }

}
