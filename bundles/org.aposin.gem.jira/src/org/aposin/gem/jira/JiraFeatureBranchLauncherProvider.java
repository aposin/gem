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
package org.aposin.gem.jira;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.launcher.IFeatureBranchLauncherProvider;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.jira.internal.config.JiraConfigBean;
import org.aposin.gem.jira.internal.config.JiraProviderConfigBean;
import org.aposin.gem.jira.internal.config.Utils;
import org.aposin.gem.jira.internal.service.JiraFeatureBranchProvider.JiraFeatureBranch;

/**
 * Provides a launcher for a JIRA feature branch (or named as <code>JIRA/*</code>
 * to open on the browser.
 */
public class JiraFeatureBranchLauncherProvider implements IFeatureBranchLauncherProvider {

    public static final String JIRA_LAUNCHER_NAME = "open_jira_ticket";
    private IConfiguration config;
    private JiraConfigBean jiraConfigBean;

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(IFeatureBranch featureBranch) {
        return Collections.emptyMap();
    }
    
    @Override
    public final String getName() {
        return "open_jira_ticket_provider";
    }

    @Override
    public final String getDisplayName() {
        return "JIRA Open Ticket";
    }

    @Override
    public List<ILauncher> getLaunchers(final IFeatureBranch featureBranch) {
        if (isBrowseSupported()) {
            final OpenJiraTicketUrl launcher = getOpenJiraTicketUrlLauncher(featureBranch);
            if (launcher != null) {
                return Collections.singletonList(launcher);
            }
        }

        // otherwise, no launchers
        return Collections.emptyList();
    }

    private OpenJiraTicketUrl getOpenJiraTicketUrlLauncher(final IFeatureBranch featureBranch) {
        if (featureBranch instanceof JiraFeatureBranch) {
            return new OpenJiraTicketUrl(featureBranch.getId(),
                    ((JiraFeatureBranch) featureBranch).getProvider().getConfigBean());
        } else {
            // otherwise, try to find an issue that is prefixed with any id
            final Optional<String> foundProviderName = jiraConfigBean.getProviderNames().stream()//
                    .filter(name -> featureBranch.getName()
                            .startsWith(name + IEnvironment.BRANCH_NAME_SEPARATOR)) //
                    .findFirst();
            if (foundProviderName.isPresent()) {
                final String providerName = foundProviderName.get();
                return new OpenJiraTicketUrl( //
                        featureBranch.getName().replace(providerName + IEnvironment.BRANCH_NAME_SEPARATOR, ""), //
                        Utils.getProviderConfig(config, providerName));
            }
        }

        return null;
    }

    @Override
    public void setConfig(final IConfiguration config) throws GemConfigurationException {
        this.config = config;
        this.jiraConfigBean = config.getPluginConfiguration("gem-jira", JiraConfigBean.class);
    }

    private static boolean isBrowseSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE);
    }

    private final class OpenJiraTicketUrl extends AbstractNoParamsLauncher {

        private final String issueKey;
        private final JiraProviderConfigBean configBean;

        public OpenJiraTicketUrl(final String issueKey, final JiraProviderConfigBean configBean) {
            this.issueKey = issueKey;
            this.configBean = configBean;
        }

        @Override
        public String getId() {
            return getName() + '_' + issueKey;
        }

        @Override
        public String getName() {
            return JIRA_LAUNCHER_NAME;
        }

        @Override
        public String getDisplayName() {
            return "JIRA ticket";
        }

        @Override
        public List<ICommand> launch() throws GemException {
            final String url = configBean.url + "/browse/" + URLEncoder.encode(issueKey, StandardCharsets.UTF_8);
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (final IOException e) {
                throw new GemException("Error opening URL: " + url);
            }
            return Collections.emptyList();
        }

        @Override
        public INamedObject getLaunchScope() {
            // scope is the JIRA group itself
            return Activator.JIRA_GROUP;
        }

        @Override
        public INamedObject getGroup() {
            return Activator.JIRA_GROUP;
        }

        @Override
        public boolean canLaunch() {
            return isBrowseSupported();
        }
    }

}
