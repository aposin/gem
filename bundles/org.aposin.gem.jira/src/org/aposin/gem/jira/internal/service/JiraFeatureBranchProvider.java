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
package org.aposin.gem.jira.internal.service;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.jira.internal.config.JiraProviderConfigBean;
import org.aposin.gem.jira.internal.config.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import io.atlassian.util.concurrent.Promise;

/**
 * Provides feature-branches based on JIRA tickets obtained by the configured
 * JIRA query (JQL).
 */
public final class JiraFeatureBranchProvider implements IFeatureBranchProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraFeatureBranchProvider.class);

    private final String providerName;

    private final Map<IEnvironment, List<IFeatureBranch>> fetchedTasks = new HashMap<>();
    private Iterable<Issue> fetchedIssues;

    private JiraProviderConfigBean configBean;

    public JiraFeatureBranchProvider(String providerName) {
        this.providerName = providerName;
    }

    public JiraProviderConfigBean getConfigBean() {
        return configBean;
    }

    @Override
    public String getId() {
        return CORE_ID_PREFIX + "jira." + getName();
    }

    @Override
    public String getName() {
        return providerName;
    }

    @Override
    public String getDisplayName() {
        return configBean.getDisplayName();
    }

    /**
     * Sets the <code>gem-jira</code> plug-in configuration for the named instance and then refresh the service.
     * @throws  
     */
    @Override
    public void setConfig(IConfiguration config) throws GemConfigurationException {
        configBean = Utils.getProviderConfig(config, providerName);
        // if configuration is set again, it should refresh
        try (JiraRestClient restClient = createJiraClient()) {
            restClient.getSessionClient().getCurrentSession().get().getLoginInfo();
            refresh();
        } catch (final Exception e) {
            if (e instanceof RestClientException) {
                handleRestClientException((RestClientException) e);
            } else if (e.getCause() instanceof RestClientException) {
                handleRestClientException((RestClientException) e.getCause());
            }
            LOGGER.error("Unexpected error for provider " + this.getId(), e);
            throw new GemConfigurationException("Unexpected error: " + e.getLocalizedMessage(), e);
        }
    }

    private void handleRestClientException(final RestClientException e) {
        LOGGER.error("REST client error for provider" + this.getId(), e);
        StringBuilder exceptionMessage = new StringBuilder("Feature Branch Provider '") //
                .append(this.getDisplayName()).append("'");
        if (e.getStatusCode().isPresent()) {
            exceptionMessage.append(": Error Code=").append(e.getStatusCode().get());
        }
        if (!e.getErrorCollections().isEmpty()) {
            e.getErrorCollections().forEach(msg -> exceptionMessage.append("\n\t- ").append(msg));
        }
        throw new GemConfigurationException(exceptionMessage.toString(), e);
    }

    @Override
    public void refresh() {
        fetchedIssues = null;
        fetchedTasks.clear();
    }

    @Override
    public List<IFeatureBranch> getFeatureBranches(final IEnvironment environment) {
        return fetchedTasks.computeIfAbsent(environment, this::doGetFeatureBranches);
    }

    private final JiraRestClient createJiraClient() {
        return new AsynchronousJiraRestClientFactory().create(URI.create(configBean.url),
                // use basic-auth but not with decrypt user/password for security
                builder -> builder.setHeader("Authorization", "Basic " + configBean.authToken));
    }

    private List<IFeatureBranch> doGetFeatureBranches(final IEnvironment environment) {
        if (fetchedIssues == null) {
            // try with resources, to close the client after fetching
            try (JiraRestClient restClient = createJiraClient()) {
                final Promise<SearchResult> result = restClient.getSearchClient().searchJql(configBean.jql);
                fetchedIssues = result.claim().getIssues();
            } catch (final Exception e) {
                // TODO: the user should be informed that the provider failed to fetch the branches
                // TODO: but this requires handling on the UI for this case
                LOGGER.error("Error fetching issues: no FB provided by " + this.getId(), e);
                LOGGER.warn("Refresh is required to retry fetching");
                fetchedIssues = Collections.emptyList();
            }
        }

        return StreamSupport.stream(fetchedIssues.spliterator(), false) //
                .map(issue -> new JiraFeatureBranch(issue, environment, this))
                .collect(Collectors.toList());
    }

    public final class JiraFeatureBranch implements IFeatureBranch {

        public final IEnvironment environment;
        public final JiraFeatureBranchProvider provider;

        public final Issue issue;

        public JiraFeatureBranch(final Issue issue, final IEnvironment environment,
                final JiraFeatureBranchProvider provider) {
            this.issue = issue;
            this.environment = environment;
            this.provider = provider;
        }

        @Override
        public String getId() {
            return issue.getKey();
        }

        @Override
        public String getSummary() {
            final String summary = issue.getSummary();
            return summary == null ? "No summary" : summary;
        }

        @Override
        public String getDescription() {
            final String description = issue.getDescription();
            return description == null ? "No description" : description;
        }

        @Override
        public IEnvironment getEnvironment() {
            return environment;
        }

        @Override
        public JiraFeatureBranchProvider getProvider() {
            return provider;
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof JiraFeatureBranch) {
                final JiraFeatureBranch other = (JiraFeatureBranch) obj;
                return Objects.equals(issue, other.issue)
                        && Objects.equals(environment, other.environment)
                        && Objects.equals(provider, other.provider);
            }

            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof IFeatureBranchProvider) {
            return compareTo((IFeatureBranchProvider) obj) == 0;
        }

        return false;
    }

}
