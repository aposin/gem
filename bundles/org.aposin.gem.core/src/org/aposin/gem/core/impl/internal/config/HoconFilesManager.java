package org.aposin.gem.core.impl.internal.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.prefs.IPreferences;
import org.aposin.gem.core.api.config.provider.IConfigFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;

/**
 * Class to manage the GEM default files used for configuration and user preferences.
 * </br>
 * This class is the responsible to read/write the files, updating at the same time
 * the in-memory representation of the same.
 */
public class HoconFilesManager implements IRefreshable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoconFilesManager.class);

    private final Config defaultConfig;
    private final IConfigFileProvider configFileProvider;
    // this config is not final to allow re-load
    private Config baseConfig;
    // this config is not final cause it is overriden and kept only in memory until persistence
    // this allows to modify the configuration without any effect on the actual file (e.g., default values)
    private Config preferences;
    // persist options for the preference file
    private ConfigRenderOptions prefPersistOptions;

    public HoconFilesManager(final IConfigFileProvider configFileProvider) {
        defaultConfig = ConfigFactory.load().resolve();
        this.configFileProvider = configFileProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        // mark preferences to reload
        preferences = null;
        baseConfig = null;
    }

    /**
     * Gets the config-file provider to access the configuration/preference paths.
     * 
     * @return config-file provider.
     */
    public IConfigFileProvider getConfigFileProvider() {
        return configFileProvider;
    }

    /**
     * Gets the preference on the ID loaded as a class.
     * 
     * @param <T> type of the preference-class.
     * @param id ID on the preference file.
     * @param prefBean bean-class to be used.
     * 
     * @return loaded preference class.
     */
    public <T> T getPreferenceBean(final String id, final Class<T> prefBean) {
        return ConfigBeanFactory.create(getPreferences().getConfig(id), prefBean);
    }
    
    /**
     * Gets the configuration on the ID loaded as a class.
     * 
     * @param <T> type of the configuration-class.
     * @param prefs preferences to be used to load the configuration.
     * @param id ID on the configuration file.
     * @param prefBean bean-class to be used.
     * 
     * @return loaded preference class.
     */
    public <T> T getConfigurationBean(final IPreferences prefs, final String id, final Class<T> configBean) {
        Config config = null;
        try {
            config = getBaseConfig(prefs).getConfig(id).resolve();
            return ConfigBeanFactory.create(config, configBean);
        } catch (final ConfigException excp) {
            if (config != null && LOGGER.isErrorEnabled()) {
                LOGGER.error("Plug-in config cannot be loaded to bean: {} {}", //
                        id, config.root().render());
            }
            throw new GemConfigurationException(
                    "Error loading " + id + " configuration: " + excp.getMessage(), excp);
        }
    }

    /**
     * Persist the preference file.
     * 
     * @throws GemException if the preference cannot be persisted.
     */
    public void persistPrefs() throws GemException {
        try {
            final String rendered = getPreferences().root().render(getPreferencesPersistOptions());
            Files.writeString(getConfigFileProvider().getPrefFile(), rendered);
        } catch (final IOException e) {
            throw new GemException("Error persisting preferences", e);
        }
    }

    // cache the persist options cause they can be re-used
    private ConfigRenderOptions getPreferencesPersistOptions() {
        if (prefPersistOptions == null) {
            final String fileName = getConfigFileProvider().getPrefFile().getFileName().toString();
            if (fileName.endsWith(".json")) {
                // json-like but formatted (user friendly)
                prefPersistOptions = ConfigRenderOptions.concise().setFormatted(true);
            } else if (fileName.endsWith(".conf")) {
                prefPersistOptions = ConfigRenderOptions.defaults().setOriginComments(false);
            } else {
                throw new GemConfigurationException("Unsupported preference file formant: " + fileName);
            }
        }
        return prefPersistOptions;
    }

    /**
     * Load a Config object from a path.
     * 
     * @param file the file to read the configuration from.
     * 
     * @return config object; {@link HoconFilesManager#defaultConfig} if path is null.
     */
    private Config loadFromFile(final Path configFile) {
        if (configFile == null) {
            return defaultConfig;
        }
        try (final Reader reader = Files.newBufferedReader(configFile)) {
            LOGGER.info("Loading configuration/preference file from {}", configFile);
            final ConfigParseOptions options = ConfigParseOptions.defaults()//
                    .prependIncluder(new PathConfigIncluder(configFile)) //
                    .setSyntaxFromFilename(configFile.toString()) //
                    .setOriginDescription(configFile + "(reader)");
            // always fallback to default configuration
            return ConfigFactory.parseReader(reader, options).withFallback(defaultConfig).resolve();
        } catch (final IOException | ConfigException e) {
            LOGGER.error("Error loading configuration/preference file", e);
            throw new GemException("Error loading the configuration/preference file", e);
        }
    }

    // helper method to be reloaded after refresh
    private Config getPreferences() {
        if (preferences == null) {
            preferences = loadFromFile(getConfigFileProvider().getPrefFile());
        }
        return preferences;
    }

    // helper method to be reloaded after refresh
    private Config getBaseConfig(final IPreferences prefs) {
        if (baseConfig == null) {
            baseConfig = loadFromFile(configFileProvider.getConfigFile(prefs));
        }
        return baseConfig;
    }

}
