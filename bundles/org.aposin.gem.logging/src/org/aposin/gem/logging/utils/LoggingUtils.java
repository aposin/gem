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
package org.aposin.gem.logging.utils;

import java.text.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the logging implementation.
 */
public final class LoggingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class);

    /**
     * Configuration file name.
     */
    public static final String CONFIG_FILE_NAME = "logback.xml";

    private LoggingUtils() {
        // cannot be instantiated.
    }

    /**
     * Configure the logging level.
     * 
     * @param level
     */
    public static void setLevel(final LogLevel level) {
        // sets the logback level for org.aposin.gem
        final ch.qos.logback.classic.Logger gemRoot =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.aposin.gem");
        gemRoot.setLevel(level.logbackLevel);
        // TODO - set other loggers, like the eclipse one?
        level.log(LOGGER, MessageFormat.format("Ussing ''{0}'' log-level", level));
    }

}
