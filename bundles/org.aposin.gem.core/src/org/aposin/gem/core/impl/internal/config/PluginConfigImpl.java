package org.aposin.gem.core.impl.internal.config;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IPluginConfiguration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigException;

public class PluginConfigImpl implements IPluginConfiguration {

    private static final String WRONG_CONFIG_FORMAT = "Wrong ''{0}'' plug-in configuration at ''{1}''";

    private final String baseId;
    private final HoconFilesManager hoconFilemanager;

    public PluginConfigImpl(final String baseId, final HoconFilesManager hoconFilemanager) {
        // TODO: maybe this can go directly to the HOCON file manager
        if (!hoconFilemanager.getBaseConfig().hasPath(baseId)) {
            throw new GemConfigurationException("Missing configuration entry for plug-in: " + baseId);
        }
        this.baseId = baseId;
        this.hoconFilemanager = hoconFilemanager;
    }

    @Override
    public boolean hasValue(String id) {
        return getPluginConfig().hasPath(id);
    }

    @Override
    public <T> T getObject(String id, Class<T> type) {
        // first handle typed values 
        if (String.class == type) {
            return (T) getString(id);
        }
        // otherwise, handle as a class
        try {
            // now handle as a class
            return ConfigBeanFactory.create(getPluginConfig().getConfig(id), type);
        } catch (ConfigException e) {
            throw new GemConfigurationException(MessageFormat.format(WRONG_CONFIG_FORMAT, baseId, id), e);
        }
    }

    @Override
    public String getString(final String id) {
        try {
            return getPluginConfig().getString(id);
        } catch (ConfigException e) {
            throw new GemConfigurationException(MessageFormat.format(WRONG_CONFIG_FORMAT, baseId, id), e);
        }
    }

    @Override
    public List<String> getStringList(String id) {
        try {
            return getPluginConfig().getStringList(id);
        } catch (ConfigException e) {
            throw new GemConfigurationException(MessageFormat.format(WRONG_CONFIG_FORMAT, baseId, id), e);
        }
    }

    @Override
    public <T> List<T> getObjectList(String id, Class<T> type) throws GemConfigurationException {
        if (String.class == type) {
            return (List<T>) getStringList(id);
        }
        // otherwise, handle as a class
        return getPluginConfig().getObjectList(id).stream() //
                .map(configObject -> ConfigBeanFactory.create(configObject.toConfig(), type)) //
                .collect(Collectors.toList());
    }

    private Config getPluginConfig() {
        // TODO: maybe this can go directly to the hoconFileManager
        return hoconFilemanager.getBaseConfig().getConfig(baseId);
    }


}
