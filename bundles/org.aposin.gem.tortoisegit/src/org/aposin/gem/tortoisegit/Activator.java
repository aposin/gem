/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur Förderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.tortoisegit;

import java.net.URI;
import java.net.URISyntaxException;

import org.aposin.gem.tortoisegit.launcher.TortoiseGitCommitLauncher;
import org.aposin.gem.tortoisegit.launcher.TortoiseGitLogLauncher;
import org.aposin.gem.tortoisegit.launcher.TortoiseGitStatusLauncher;
import org.aposin.gem.ui.theme.ThemeIconRegistry;
import org.aposin.gem.ui.theme.icons.GemSvgIcon;
import org.aposin.gem.ui.theme.icons.IGemIcon;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final String TORTOISE_ICON = "icons/tgit_logo.svg";

    public static Logger LOGGER;

    private static BundleContext context;

    public static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        Activator.LOGGER = LoggerFactory.getLogger(this.getClass().getPackageName());
        registerTortoiseIcon();
    }

    private static final void registerTortoiseIcon() {
        // register also the icon
        try {
            final URI iconUri = getContext().getBundle().getResource(TORTOISE_ICON).toURI();
            // TODO - requires theming?
            final IGemIcon icon = new GemSvgIcon(TORTOISE_ICON, iconUri);
            // registering by command name
            ThemeIconRegistry.getInstance()
                    .registerIconByName(TortoiseLauncherProvider.LAUNCHER_NAME_PREFIX + //
                            TortoiseGitCommitLauncher.COMMAND_NAME, icon);
            ThemeIconRegistry.getInstance()
                    .registerIconByName(TortoiseLauncherProvider.LAUNCHER_NAME_PREFIX + //
                            TortoiseGitLogLauncher.COMMAND_NAME, icon);
            ThemeIconRegistry.getInstance()
                    .registerIconByName(TortoiseLauncherProvider.LAUNCHER_NAME_PREFIX + //
                            TortoiseGitStatusLauncher.COMMAND_NAME, icon);
            // also register the group for the launchers, that is the launcher-provider
            ThemeIconRegistry.getInstance()
                     .registerIconByName(TortoiseLauncherProvider.class.getName(), icon);
        } catch (final URISyntaxException e) {
            Activator.LOGGER.error("Error loading icon {}: {}", TORTOISE_ICON, e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        Activator.LOGGER = null;
    }

}
