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
package org.aposin.gem.ui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator class.
 */
public class Activator implements BundleActivator {

    public static final String PLUGIN_ID = "org.aposin.gem.ui";
    
    /**
     * User path for this application.
     */
    public static final java.nio.file.Path APP_USER_PATH =
            Paths.get(System.getProperty("user.home"), "GEM");

    
    /**
     * Logger for whole UI layer.
     */
    public static Logger LOGGER;

    private static BundleContext BUNDLE_CONTEXT;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = bundleContext;
        LOGGER = LoggerFactory.getLogger("org.aposin.gem");
        createUserPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = null;
        LOGGER = LoggerFactory.getLogger("org.aposin.gem");
    }

    /**
     * Creates the user path on startup.
     */
    public static void createUserPath() {
        try {
            Files.createDirectories(APP_USER_PATH);
        } catch (IOException e) {
            LOGGER.error("Cannot create application user path: {}", APP_USER_PATH);
        }
    }

    public static String getBundleSymbolicName() {
        return BUNDLE_CONTEXT.getBundle().getSymbolicName();
    }

    /**
     * Gets the Vendor for the bundle.
     * 
     * @return bundle-vendor.
     */
    public static String getVendor() {
        return BUNDLE_CONTEXT.getBundle().getHeaders().get(Constants.BUNDLE_VENDOR);
    }

    /**
     * Gets the Version for the bundle.
     * 
     * @return version.
     */
    public static Version getVersion() {
        return BUNDLE_CONTEXT.getBundle().getVersion();
    }

    public static URI getResource(final String name) {
        try {
            return BUNDLE_CONTEXT.getBundle().getResource(name).toURI();
        } catch (final NullPointerException | URISyntaxException e) {
            LOGGER.warn("Resource not found {}", name);
            LOGGER.debug("Exception", e);
            return null;
        }
    }
}
