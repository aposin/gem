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
package org.aposin.gem.github;

import java.net.URI;
import java.net.URISyntaxException;

import org.aposin.gem.github.launcher.OpenBranchLauncher;
import org.aposin.gem.github.launcher.PullRequestLauncher;
import org.aposin.gem.github.service.GithubLauncherProvider;
import org.aposin.gem.ui.theme.ThemeConstants;
import org.aposin.gem.ui.theme.ThemeIconRegistry;
import org.aposin.gem.ui.theme.icons.GemSvgIcon;
import org.aposin.gem.ui.theme.icons.IGemIcon;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    /**
     * Logger for the Plug-in.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Activator.class.getPackageName());

    public static final String ICON_FONTAWESOME_GITHUB_SQUARE =
            "icons/fontawesome/github-square.svg";

    private static BundleContext BUNDLE_CONTEXT;

    static BundleContext getContext() {
        return BUNDLE_CONTEXT;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = bundleContext;
        registerGithubIcon();
    }

    private static final void registerGithubIcon() {
        // register also the icon
        try {
            final URI iconUri =
                    BUNDLE_CONTEXT.getBundle().getResource(ICON_FONTAWESOME_GITHUB_SQUARE).toURI();
            // use default core stylesheet
            final IGemIcon icon = new GemSvgIcon(ICON_FONTAWESOME_GITHUB_SQUARE, iconUri, null,
                    ThemeConstants.STYLESHEET_MAP);
            // registering by command name
            ThemeIconRegistry.getInstance().registerIconByName(OpenBranchLauncher.LAUNCHER_NAME,
                    icon);
            ThemeIconRegistry.getInstance().registerIconByName(PullRequestLauncher.LAUNCHER_NAME,
                    icon);
            // and also the group name of the launchers
            ThemeIconRegistry.getInstance().registerIconByName(GithubLauncherProvider.NAME, icon);
        } catch (final URISyntaxException e) {
            LOGGER.error("Error loading icon {}: {}", ICON_FONTAWESOME_GITHUB_SQUARE, e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = null;
    }
}
