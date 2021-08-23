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
package org.aposin.gem.logging;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.aposin.gem.logging.utils.LoggingUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Activator implements BundleActivator {

    public static final String CONFIG_FILE_PROPERTY = "org.aposin.gem.logging.config_file";

    /**
     * Logger for the plug-in.
     */
    public static Logger LOGGER;

    private static BundleContext BUNDLE_CONTEXT;
    private static URL DEFAULT_CONFIG_FILE;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = bundleContext;
        configureLogger();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        BUNDLE_CONTEXT = null;
    }

    /**
     * Configures the logger.
     */
    public static void configureLogger() throws IOException {
        final URL configFile = getConfigUri();
        try (final InputStream configStream = configFile.openStream()) {
            final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            final JoranConfigurator jc = new JoranConfigurator();
            jc.setContext(context);
            context.reset();
            try {
                jc.doConfigure(configStream);
            } catch (final JoranException e) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(context);
            }
        }
        // logger for this plug-in
        LOGGER = LoggerFactory.getLogger(Activator.class.getPackageName());
        LOGGER.debug("Initialized logger with {}", configFile);
    }


    private static URL getConfigUri() {
        final String configFileProperty = System.getProperty(CONFIG_FILE_PROPERTY);
        if (configFileProperty != null) {
            try {
                return new URL(configFileProperty);
            } catch (final MalformedURLException e) {
                // TODO - is it alright on the System.err?
                System.err.println("Wrong config file for logging: using default");
                e.printStackTrace();
            }
        }

        return getDefaultConfig();
    }

    public static void setDefaultConfig(final URL configUri) {
        if (DEFAULT_CONFIG_FILE != null) {
            throw new IllegalStateException(
                    "Default configuration already set to " + DEFAULT_CONFIG_FILE);
        }
        DEFAULT_CONFIG_FILE = configUri;
    }

    private static URL getDefaultConfig() {
        if (DEFAULT_CONFIG_FILE == null) {
            setDefaultConfig(BUNDLE_CONTEXT.getBundle().getResource(LoggingUtils.CONFIG_FILE_NAME));
        }
        return DEFAULT_CONFIG_FILE;
    }

}
