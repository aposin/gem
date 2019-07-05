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
package org.aposin.gem.oomph;

import java.net.URI;
import java.net.URISyntaxException;

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

    private static final String SIMPLEICONS_ECLIPSEIDE = "icons/simpleicons/eclipseide.svg";

    private static BundleContext BUNDLE_CONTEXT;

    @Override
    public void start(BundleContext context) throws Exception {
        BUNDLE_CONTEXT = context;
        registerEclipseIcon();
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        BUNDLE_CONTEXT = null;
    }

    private static final void registerEclipseIcon() {
        // register also the icon
        try {
            final URI iconUri =
                    BUNDLE_CONTEXT.getBundle().getResource(SIMPLEICONS_ECLIPSEIDE).toURI();
            // use default core stylesheet
            final IGemIcon icon = new GemSvgIcon(SIMPLEICONS_ECLIPSEIDE, iconUri, null,
                    ThemeConstants.STYLESHEET_MAP);
            // registering by command name
            ThemeIconRegistry.getInstance().registerIconByName(EclipseLauncher.LAUNCHER_NAME, icon);
        } catch (final URISyntaxException e) {
            LOGGER.error("Error loading icon {}: {}", SIMPLEICONS_ECLIPSEIDE, e);
        }
    }

}
