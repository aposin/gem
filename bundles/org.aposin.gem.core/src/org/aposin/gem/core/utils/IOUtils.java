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
package org.aposin.gem.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to work with files.
 */
public final class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
        // cannot be instantiated - utility class
    }

    /**
     * Writes the content of an URI to the target path.
     * 
     * @param uri resource URI.
     * @param target path to copythe content.
     * @throws IOException if any IO error occurs.
     */
    public static final void writeContent(final URI uri, final Path target) throws IOException {
        try (final InputStream is = uri.toURL().openStream()) {
            Files.copy(is, target);
        }
    }

    /**
     * Converts the given path to a String, replacing backslashes against slashes.
     * </br>
     * Windows uses backslashes, so replace them against slashes for Oomph. This has no effect on
     * Linux and Mac as there does not exist backslashes. So executing this on all platforms should
     * be OK.
     * 
     * @param path the path to convert
     * @return the path as {@link String}
     */
    public static final String pathToString(final Path path) {
        return path.toString().replace('\\', '/');
    }

    /**
     * Deletes the file and ignores any {@link IOException}.
     * </br>
     * Ignored exceptions are logged.
     * 
     * @param path path to delete.
     */
    public static final void deleteIgnoringErrors(final Path path) {
        // do not try to delete non-exisiting files
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (final Exception e) {
                LOGGER.warn("Ignoring error while deleting file: {}", path);
                LOGGER.debug("Exception deleting path", e);
            }
        }
    }

    /**
     * Deletes recursively ignoring any {@link IOException}.
     * </br>
     * Ignored exceptions are logged.
     * 
     * @param dir directory to remove recursively.
     */
    public static final void deleteRecursivelyIgnoringErrors(final Path dir) {
        if (Files.exists(dir)) {
            try (final Stream<Path> fileStream = Files.walk(dir)) {
                fileStream.sorted(Comparator.reverseOrder()).forEach(IOUtils::deleteIgnoringErrors);
            } catch (final Exception e) {
                LOGGER.warn("Ignoring error while deleting directory recursively: {}", dir);
                LOGGER.debug("Exception deleting directory", e);
            }
        }
    }

}
