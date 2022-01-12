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
package org.aposin.gem.core.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to find an executable on the <em>PATH</em>.
 */
public final class ExecUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecUtils.class);

    private static final List<String> WINDOWS_EXEC_EXTENSIONS =
            Arrays.asList(".exe", ".cmd", ".bat");

    private ExecUtils() {
        // cannot be instantiated - utility class
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    /**
     * Executes the specified command and arguments in a separate process.
     * </br>
     * Similar to {@link Runtime#exec(String[])}, but discarding stdout/stderr.
     * 
     * @param cmdArgs command and arguments tokenized and quoted if required.
     * @param directory the process working directory.
     * @return running process.
     * @throws IOException if the command fails to launch
     */
    public static Process exec(final List<String> cmdArgs, Path directory) throws IOException {
        return new ProcessBuilder(cmdArgs) //
            .directory(directory == null ? null : directory.toFile())
            .redirectOutput(Redirect.DISCARD) //
            .redirectError(Redirect.DISCARD) //
            .start();
    }
    
    /**
     * Try to find the executable (by name) on the Path.
     * 
     * @param name the name of the binary.
     * @return the absolute path to the binary; {@code null} otherwise.
     */
    public static Path findExecutable(final String name) {
        final List<Path> pathDirectories = getLookupPaths();
        final Path binary = findOnDirectories(pathDirectories, name);
        if (binary != null) {
            return binary;
        }

        // if it is running on windows, try with the other paths
        if (isWindows()) {
            for (final String ext : WINDOWS_EXEC_EXTENSIONS) {
                final Path windowsBinary = findOnDirectories(pathDirectories, name + ext);
                if (windowsBinary != null) {
                    return windowsBinary;
                }
            }
        }

        return null;
    }

    private static final List<Path> getLookupPaths() {
        final String[] pathEntries = System.getenv("PATH").split(File.pathSeparator);
        final List<Path> pathDirectories = new ArrayList<>(pathEntries.length);
        for (final String pathEntry : pathEntries) {
            try {
                final Path path = Paths.get(pathEntry);
                if (Files.isDirectory(path)) {
                    pathDirectories.add(path);
                } else {
                    LOGGER.warn("Ignoring lookup path {}: not directory", pathEntry);
                }
            } catch (final InvalidPathException e) {
                LOGGER.warn("Ignoring lookup path {}: {}", pathEntry, e.getLocalizedMessage());
            }
        }

        return pathDirectories;
    }

    private static final Path findOnDirectories(final List<Path> lookupDirs, final String name) {
        for (final Path lookupPath : lookupDirs) {
            final Path path = lookupPath.resolve(name);
            if (Files.exists(path) && Files.isExecutable(path)) {
                return path;
            }
        }
        return null;
    }
}
