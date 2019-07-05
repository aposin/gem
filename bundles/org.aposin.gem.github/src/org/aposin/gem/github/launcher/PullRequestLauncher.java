/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur Förderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.github.launcher;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.github.service.GithubLauncherProvider;

/**
 * Launcher to open in the browser the GitHub Pull-Request
 * for the feature branch.
 * 
 * @see https://help.github.com/en/github/managing-your-work-on-github/about-automation-for-issues-and-pull-requests-with-query-parameters
 */
public class PullRequestLauncher extends AbstractNoParamsLauncher {

    /**
     * Laucnher name to open GitHub Pull Request.
     */
    public static final String LAUNCHER_NAME = "github_pull_request";

    private final GithubLauncherProvider provider;
    private final IWorktreeDefinition worktreeDefinition;
    private final IFeatureBranch featureBranch;

    public PullRequestLauncher(final GithubLauncherProvider provider,
            final IWorktreeDefinition worktreeDefinition, final IFeatureBranch featureBranch) {
        this.provider = provider;
        this.worktreeDefinition = worktreeDefinition;
        this.featureBranch = featureBranch;
    }

    @Override
    public String getId() {
        return getName() + '_' + getLaunchScope().getName() + '_' + featureBranch.getName();
    }

    @Override
    public String getName() {
        return LAUNCHER_NAME;
    }

    @Override
    public String getDisplayName() {
        return "PullRequest";
    }

    @Override
    public INamedObject getGroup() {
        return provider;
    }

    @Override
    public IRepository getLaunchScope() {
        return worktreeDefinition.getRepository();
    }

    @Override
    public boolean canLaunch() {
        return GithubLauncherProvider.supportsDesktop() && //
                !featureBranch.getEnvironment().getWorkflow().getCloneLauncher().canLaunch() && //
                !featureBranch.getEnvironment().getWorkflow().getSetupWorktreeLauncher().canLaunch() && //
                !featureBranch.getWorkflow().getFetchAndCheckoutLauncher().canLaunch();
    }


    @Override
    public List<ICommand> launch() throws GemException {
        String url = MessageFormat.format("{0}/compare/{1}...{2}?quick_pull=1", //
                getLaunchScope().getServer(), //
                URLEncoder.encode(
                        featureBranch.getEnvironment().getEnvironmentBranch(getLaunchScope()),
                        StandardCharsets.UTF_8), //
                URLEncoder.encode(featureBranch.getCheckoutBranch(getLaunchScope()),
                        StandardCharsets.UTF_8));
        try {
            // TODO - add support for some extra configuration (e.g, labels?)
            // TODO - instead of DisplayName, add new method to get commit title and commit
            // description?
            // TODO - if so, should be used in tortoise too
            url = addQuery(url, "title", featureBranch.getDisplayName());
            url = addQuery(url, "body", featureBranch.getDescription());
            // launch the URI
            Desktop.getDesktop().browse(new URI(url));
        } catch (final URISyntaxException | IOException e) {
            throw new GemException("Error opening Pull-Request url: " + url, e);
        }

        return Collections.emptyList();
    }

    private static final String addQuery(final String url, final String param, final String value) {
        if (value == null || value.isBlank()) {
            return url;
        }
        return url + String.format("&%s=%s", //
                param, //
                URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

}
