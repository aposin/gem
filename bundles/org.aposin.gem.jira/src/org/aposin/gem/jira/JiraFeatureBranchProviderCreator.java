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

import java.util.List;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.service.IGemServiceCreator;
import org.aposin.gem.jira.internal.config.JiraConfigBean;
import org.aposin.gem.jira.internal.config.Utils;
import org.aposin.gem.jira.internal.service.JiraFeatureBranchProvider;
import org.osgi.service.component.annotations.Component;

@Component(service = IGemServiceCreator.class)
public class JiraFeatureBranchProviderCreator implements IGemServiceCreator<IFeatureBranchProvider>, IRefreshable {

    private JiraConfigBean configBean;
    private List<IFeatureBranchProvider> providers;

    @Override
    public Class<IFeatureBranchProvider> getType() {
        return IFeatureBranchProvider.class;
    }

    @Override
    public String getName() {
        return "jira_fb_providers_creator";
    }

    @Override
    public String getDisplayName() {
        return "JIRA FB Providers";
    }

    @Override
    public void setConfig(IConfiguration config) throws GemConfigurationException {
        this.configBean = Utils.getJiraConfig(config);
    }

    @Override
    public void refresh() {
        this.configBean = null;
        this.providers = null;
    }

    @Override
    public List<IFeatureBranchProvider> createServices() {
        if (providers == null) {
            providers = configBean.getProviderNames().stream() //
                    .map(JiraFeatureBranchProvider::new) //
                    .collect(Collectors.toList());
        }
        return providers;
    }

}
