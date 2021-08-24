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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.service.launcher.IEnvironmentLauncherProvider;
import org.aposin.gem.oomph.impl.internal.config.bean.GemEclipseBean;
import org.osgi.service.component.annotations.Component;

@Component(service = IEnvironmentLauncherProvider.class)
public class EclipseLauncherProvider implements IEnvironmentLauncherProvider {

    /* package */ protected GemEclipseBean eclipseBean;

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
        this.eclipseBean = config.getPluginConfiguration("eclipse", GemEclipseBean.class);
    }

    @Override
    public List<ILauncher> getLaunchers(IEnvironment environment) {
        final int nReleases = eclipseBean.releases.size();
        final List<ILauncher> starters = new ArrayList<>(nReleases);
        for (int idx = 0; idx < nReleases; idx++) {
            // TODO - implement a filter on the configuration for environments/projects
            starters.add(new EclipseLauncher(this, environment, idx));
        }
        return starters;
    }

    @Override
    public Map<IRepository, List<ILauncher>> getRepositoryLaunchers(final IEnvironment environment) {
        return Collections.emptyMap();
    }

}
