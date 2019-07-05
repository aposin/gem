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
package org.aposin.gem.ui.theme;

import java.util.HashMap;
import java.util.Map;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.impl.service.launcher.GitFeatureBranchLauncherProvider;
import org.aposin.gem.core.impl.service.launcher.OpenEnvironmentLauncherProvider;
import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.process.launcher.CopyToClipboardLauncher;
import org.aposin.gem.ui.process.launcher.NullLauncher;
import org.aposin.gem.ui.process.service.MisconfiguredLauncherProvider;
import org.aposin.gem.ui.theme.icons.GemSvgIcon;
import org.aposin.gem.ui.theme.icons.IGemIcon;
import org.aposin.gem.ui.theme.icons.IconResources;

/**
 * Registry for icons for named objects.
 */
public final class ThemeIconRegistry {

    private static ThemeIconRegistry INSTANCE;

    // map with shared core icons by resource location
    private Map<String, IGemIcon> sharedCoreIcons = new HashMap<>();
    
    private Map<String, IGemIcon> byObjectIdRegistry = new HashMap<>();
    private Map<String, IGemIcon> byObjectNameRegistry = new HashMap<>();

    private ThemeIconRegistry() {
        // cannot be instantiated (singleton)
    }

    public static final ThemeIconRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ThemeIconRegistry();
            INSTANCE.registerCoreIcons();
        }
        return INSTANCE;
    }

    /**
     * This are the core icons registered by the <em>org.opin.gem.ui</em> plug-in.
     * </br>
     * As they are registered on startup, they could be overriden by later contributors.
     */
    private void registerCoreIcons() {
        registerIconByName(OpenEnvironmentLauncherProvider.OPEN_SERVER_LAUNCHER_NAME,
                getOrCreateCoreIcon(IconResources.FONTAWESOME_CLOUD_SVG));
        registerIconByName(GitFeatureBranchLauncherProvider.PUSH_LAUNCHER_NAME, //
                getOrCreateCoreIcon(IconResources.FONTAWESOME_CLOUD_UPLOAD_ALT_SVG));
        registerIconByName(OpenEnvironmentLauncherProvider.OPEN_WORKTREE_LAUNCHER_NAME,
                getOrCreateCoreIcon(IconResources.FONTAWESOME_FOLDER_OPEN_SVG));
        registerIconByName(CopyToClipboardLauncher.NAME, //
                getOrCreateCoreIcon(IconResources.FONTAWESOME_COPY_SVG));
        // exclamation for several error icons
        registerIconByName(MisconfiguredLauncherProvider.LAUNCHER_NAME,
                getOrCreateCoreIcon(IconResources.FONTAWESOME_EXCLAMATION_SVG));
        registerIconByName(NullLauncher.NAME, //
                getOrCreateCoreIcon(IconResources.FONTAWESOME_EXCLAMATION_SVG));
    }

    /**
     * Helper method to share core icons for different registers.
     * 
     * @param resourceLocation location for the SVG icon
     */
    private IGemIcon getOrCreateCoreIcon(final String resourceLocation) {
        return sharedCoreIcons.computeIfAbsent(resourceLocation, //
                location -> new GemSvgIcon(location, //
                    Activator.getResource(location), //
                    null, // default to bare icon
                    ThemeConstants.STYLESHEET_MAP));
    }

    /**
     * Register an icon by ID.
     * 
     * @param id
     * @param icon
     */
    public void registerIconById(final String id, final IGemIcon icon) {
        byObjectIdRegistry.put(id, icon);
    }

    /**
     * Register the icon by name.
     * 
     * @param name
     * @param icon
     */
    public void registerIconByName(final String name, final IGemIcon icon) {
        byObjectNameRegistry.put(name, icon);
    }

    /**
     * Gets the icon for a named object.
     * </br>
     * Firs it retrieves the icon by ID and then by name.
     * 
     * @param namedObject object to get the icon.
     * @return icon registered for the object; {@link IGemIcon#NULL_ICON} if none.
     */
    public IGemIcon getIcon(final INamedObject namedObject) {
        IGemIcon icon = getIconById(namedObject.getId());
        if (icon == IGemIcon.NULL_ICON) {
            icon = getIconByName(namedObject.getName());
        }
        return icon;
    }
    
    /**
     * Gets the registered icon by ID.
     * 
     * @param id icon ID.
     * @return registered icon.
     */
    public IGemIcon getIconById(final String id) {
        return byObjectIdRegistry.getOrDefault(id, IGemIcon.NULL_ICON);
    }
    
    /**
     * Gets the registered icon by name.
     * 
     * @param id icon name.
     * @return registered icon.
     */
    public IGemIcon getIconByName(final String name) {
        return byObjectNameRegistry.getOrDefault(name, IGemIcon.NULL_ICON);
    }

}
