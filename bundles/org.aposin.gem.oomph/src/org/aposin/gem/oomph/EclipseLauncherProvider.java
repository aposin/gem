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
package org.aposin.gem.oomph;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.config.IPluginConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

@Component(service = IEnvironmentLauncherProvider.class)
public class EclipseLauncherProvider implements IEnvironmentLauncherProvider {

    private static final String OOMPH_PATH_KEY = "oomphpath";
    private static final String PRODUCT_CATALOG_KEY = "productCatalog";
    private static final String PROJECT_CATALOG_KEY = "projectCatalog";
    private static final String BUNDLE_POOL_CONFIG_KEY = "bundlePoolConfig";
    private static final String BUNDLE_POOL_KEY = "bundlePool";
    private static final String RELEASES_KEY = "releases";

    // TODO: make package protected
    Path oomphPath;
    String productCatalog;
    String projectCatalog;
    List<EclipseRelease> releases;
    // this are optional configuration entries
    Optional<Path> bundlePool;
    Path bundlePoolConfig; // this can be null if the bundlePool is not configured

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public String getDisplayName() {
        return "Eclipse";
    }

    @Override
    public void setConfig(final IConfiguration config) {
        IPluginConfiguration pluginConfiguration = config.getPluginConfiguration("eclipse");
        // required
        // ommphpath configuration is relative to the config file
        this.oomphPath = config.getRelativeToConfigFile(pluginConfiguration.getString("oomphpath"));
        this.productCatalog = pluginConfiguration.getString("productCatalog");
        this.projectCatalog = pluginConfiguration.getString("projectCatalog");
        // releases should not be empty
        this.releases = pluginConfiguration.getObjectList("releases", EclipseRelease.class);
        if (releases.isEmpty()) {
            throw new GemConfigurationException("No eclipse releases configured");
        }
        // optional bundle pool
        if (pluginConfiguration.hasValue(BUNDLE_POOL_KEY)) {
            this.bundlePool = Optional.of(Paths.get(pluginConfiguration.getString(BUNDLE_POOL_KEY)));
            if (pluginConfiguration.hasValue(BUNDLE_POOL_CONFIG_KEY)) {
                this.bundlePoolConfig = Paths.get(pluginConfiguration.getString(BUNDLE_POOL_CONFIG_KEY));
                // TODO: not sure why this was required
                // Split slash and backslash
                //                final String[] bundlePoolConfigSplit = bundlePoolConfigString.split("[/\\\\]");
                //                bundlePoolConfigPath = Paths.get("", //
                //                        Arrays.stream(bundlePoolConfigSplit) //
                //                                .map(EclipseLauncher::mapSystemProperty)
                //                                .toArray(String[]::new));
            } else {
                // set the default value
                this.bundlePoolConfig = Paths.get(System.getProperty("user.home"), ".eclipse",
                        "org.eclipse.oomph.p2");
            }
            

        } else {
            LoggerFactory.getLogger(this.getClass())
                    .warn("Optional bundle pool configuration missing: ignoring related config");
            this.bundlePool = Optional.empty();
            this.bundlePoolConfig = null;
        }
        
    }

    @Override
    public List<ILauncher> getLaunchers(IEnvironment environment) {
        final int nReleases = releases.size();
        final List<ILauncher> starters = new ArrayList<>(nReleases);
        for (int idx = 0; idx < nReleases; idx++) {
            // TODO #42 - implement a filter on the configuration for environments/projects
            starters.add(new EclipseLauncher(this, environment, idx));
        }
        return starters;
    }

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IEnvironment environment) {
        return Collections.emptyMap();
    }

    /**
     * Eclipse release POJO.
     */
    public static class EclipseRelease {

        public String name;
        public String displayname;
        public List<String> paths;
        public List<String> args;
        public List<String> vmargs;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayname() {
            return displayname;
        }

        public void setDisplayname(String displayname) {
            this.displayname = displayname;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getArgs() {
            return args;
        }

        public void setArgs(List<String> args) {
            this.args = args;
        }

        public List<String> getVmargs() {
            return vmargs;
        }

        public void setVmargs(List<String> vmargs) {
            this.vmargs = vmargs;
        }

    }
    
}
