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
package org.aposin.gem.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator for the bundle.
 */
public class Activator implements BundleActivator {

    /**
     * Logger for the bundle package.
     */
    public static Logger LOGGER;

    private static BundleContext BUNDLE_CONTEXT;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = bundleContext;
        LOGGER = LoggerFactory.getLogger(this.getClass().getPackageName());
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        BUNDLE_CONTEXT = null;
        LOGGER = null;
    }

    /**
     * Gets the bundle context.
     * 
     * @return bundle context.
     */
    public static BundleContext getBundleContext() {
        return BUNDLE_CONTEXT;
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
