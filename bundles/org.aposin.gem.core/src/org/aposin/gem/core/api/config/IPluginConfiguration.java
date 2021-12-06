package org.aposin.gem.core.api.config;

import java.util.List;

/**
 * Interface for the plug-in configuration.
 */
// TODO: maybe extends IConfigurable to get the configuration?
public interface IPluginConfiguration {

    public boolean hasValue(final String id);

    /**
     * Gets the configuration value as an object of the provided type.
     * </br>
     * NOTE: the type should either one of the supported json types
     * or a POJO with getters and setters.
     * 
     * @param <T>
     * @param id
     * @param type
     * @return
     */
    public <T> T getObject(final String id, final Class<T> type) throws GemConfigurationException;

    /**
     * Gets the configuration value as a string.
     * 
     * @param id
     * @return
     * @throws GemConfigurationException
     */
    public String getString(final String id) throws GemConfigurationException;

    /**
     * Gets the configuration value as a list of string.
     * 
     * @param id
     * @return
     * @throws GemConfigurationException
     */
    public List<String> getStringList(final String id) throws GemConfigurationException;

    public <T> List<T> getObjectList(final String id, final Class<T> type) throws GemConfigurationException;

}
