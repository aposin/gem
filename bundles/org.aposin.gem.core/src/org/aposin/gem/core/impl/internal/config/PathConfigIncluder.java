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
package org.aposin.gem.core.impl.internal.config;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigIncludeContext;
import com.typesafe.config.ConfigIncluder;
import com.typesafe.config.ConfigIncluderFile;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOriginFactory;

/**
 * {@link ConfigIncluderFile} to include files based on a root path.
 * </br>
 * This is necessary as the typesafe-config framework supports only files,
 * and thus from {@link Path} a {@link Reader} should be used.
 * </br>
 * This avoids to use other filesystem abstractions, that might be useful to
 * get configuration from other resources (e.g., HTPP urls).
 */
public class PathConfigIncluder implements ConfigIncluder, ConfigIncluderFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathConfigIncluder.class);
    private final Path root;
    private final ConfigIncluder fallback;

    /**
     * Constructor with the root path.
     * 
     * @param root path to resolve configuration against.
     */
    public PathConfigIncluder(final Path root) {
        this(root, null);
    }

    /**
     * Constructor to add a fallback to the includer.
     * 
     * @param root path to resolve configuration against.
     * @param fallback fallback in case it cannot be resolved.
     */
    public PathConfigIncluder(final Path root, final ConfigIncluder fallback) {
        this.root = root;
        this.fallback = fallback;
    }

    private ConfigObject includePath(final ConfigIncludeContext context, final Path path) {
        ConfigObject obj = null;
        try (final Reader reader = Files.newBufferedReader(path)) {
            obj = ConfigFactory.parseReader(reader, context.parseOptions()).root();
            // if there is a fallback that is a PathConfigIncluder
            if (fallback instanceof PathConfigIncluder) {
                return obj.withFallback(((PathConfigIncluder) fallback).includePath(context, path));
            }
        } catch (final IOException e) {
            if (!context.parseOptions().getAllowMissing()) {
                throw new ConfigException.IO(ConfigOriginFactory.newFile(path.toString()),
                        e.getMessage(), e);
            }
        }
        return obj;
    }

    /**
     * Implementation ignores any fallback file includer, as it might fail if 
     * the {@link Path} implementation is used but it will still work if the root
     * is properly configured.
     */
    @Override
    public ConfigObject includeFile(final ConfigIncludeContext context, final File file) {
        // only use the includePath for files, ignore any other ConfigIncluderFile
        if (fallback instanceof ConfigIncluderFile) {
            LOGGER.warn("Ignoring file includer: {}", fallback);
        }
        return includePath(context, root.resolveSibling(file.toPath()));
    }

    @Override
    public ConfigObject include(final ConfigIncludeContext context, final String name) {
        ConfigObject obj = null;
        final Path path = root.resolveSibling(name);
        if (Files.exists(path)) {
            obj = includePath(context, path);
        }

        if (fallback != null) {
            final ConfigObject fallbackObject = fallback.include(context, name);
            if (obj == null) {
                return fallbackObject;
            } else {
                obj.withFallback(fallbackObject);
            }
        }

        return obj;
    }

    @Override
    public ConfigIncluder withFallback(ConfigIncluder fallback) {
        if (this == fallback) {
            throw new ConfigException.BugOrBroken("trying to create includer cycle");
        } else if (this.fallback == fallback) {
            return this;
        } else if (this.fallback != null) {
            return new PathConfigIncluder(root, fallback.withFallback(fallback));
        } else {
            return new PathConfigIncluder(root, fallback);
        }
    }

}
