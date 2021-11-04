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

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.github.service.GithubLauncherProvider;

/**
 * Launcher to open GitHub on the browser GitHub
 * on the branch-tree for the repository.
 */
public class OpenBranchLauncher extends AbstractNoParamsLauncher {

    /**
     * Launcher name to open GitHub Branch.
     */
    public static final String LAUNCHER_NAME = "open_github_branch";

    private final GithubLauncherProvider provider;
    private final IRepository repository;
    private final String branchName;

    /**
     * Default constructor.
     * 
     * @param provider launcher provider (group).
     * @param repository GitHub repository.
     * @param branchName name of the branch to open.
     */
    public OpenBranchLauncher(final GithubLauncherProvider provider, final IRepository repository,
            final String branchName) {
        this.provider = provider;
        this.repository = repository;
        this.branchName = branchName;
    }

    @Override
    public String getName() {
        return LAUNCHER_NAME;
    }

    @Override
    public String getDisplayName() {
        return MessageFormat.format("{0}@{1}", repository.getDisplayName(), branchName);
    }

    @Override
    public INamedObject getGroup() {
        return provider;
    }

    @Override
    public INamedObject getLaunchScope() {
        return repository;
    }

    @Override
    public boolean canLaunch() {
        return GithubLauncherProvider.supportsDesktop();
    }

    @Override
    public List<ICommand> launch() throws GemException {
        final String url = repository.getServer() + "/tree/"
                + URLEncoder.encode(branchName, StandardCharsets.UTF_8);
        try {
            // launch the URI
            Desktop.getDesktop().browse(new URI(url));
        } catch (final URISyntaxException | IOException e) {
            throw new GemException("Error opening Pull-Request url: " + url, e);
        }

        return Collections.emptyList();
    }

}
