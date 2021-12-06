package org.aposin.gem.jira.internal.config;

import java.util.List;

import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.config.IPluginConfiguration;
import org.aposin.gem.core.utils.ConfigUtils;

/**
 * JIRA provider configuration.
 */
public class JiraProviderConfig {

    private static final String GEM_JIRA_KEY = "gem-jira";
    // only the names of the providers to then build each instance
    private static final String PROVIDER_NAMES = "providerNames";
    // for each provider, these are the keys for each field
    private static final String DISPLAYNAME_KEY = "displayName";
    private static final String URL_KEY = "url";
    private static final String AUTH_TOKEN_KEY = "authToken";
    private static final String JQL_KEY = "jql";

    /**
     * The displayName for the JIRA instance.
     */
    public final String displayName;
    /**
     * URL for JIRA.
     */
    public final String url;
    /**
     * Token for Basic authentication.
     */
    public final String authToken;
    /**
     * Jira Query (in JQL).
     */
    public final String jql;

    private JiraProviderConfig(String displayName, String url, String authToken, String jql) {
        this.displayName = displayName;
        this.url = url;
        this.authToken = authToken;
        this.jql = jql;
    }

    public static List<String> getProviderNames(final IConfiguration config) {
        final List<String> providerNames = config.getPluginConfiguration(GEM_JIRA_KEY).getStringList(PROVIDER_NAMES);
        if (providerNames.isEmpty()) {
            throw new GemConfigurationException("No JIRA providers configured");
        }
        return providerNames;
    }

    public static JiraProviderConfig getProviderConfig(final IConfiguration config, final String name) {
        final IPluginConfiguration jiraConfig = config.getPluginConfiguration(ConfigUtils.joinIds(GEM_JIRA_KEY, name));
        return new JiraProviderConfig(//
                jiraConfig.hasValue(DISPLAYNAME_KEY) ? jiraConfig.getString(DISPLAYNAME_KEY) : name, //
                jiraConfig.getString(URL_KEY), //
                jiraConfig.getString(AUTH_TOKEN_KEY), //
                // TODO - this JQL might contain some bindings for environment filtering
                // TODO - think about how to handle it (might be useful also for scripting)
                // TODO - or maybe a default JQL and a env_id->JQL
                // TODO - this can maybe be done maybe with the preference framework within the config object instead of load here
                jiraConfig.getString(JQL_KEY));
    }

}
