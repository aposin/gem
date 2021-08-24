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
package org.aposin.gem.ui.process.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.workflow.ICommand;

/**
 * Launcher wrapper to join several launchers on a single one.
 * </br>
 * WARNING: it only supports by now launchers without params!
 * </br>
 * This is supposed to be used just to join equal launchers
 * (for example, by repository). Otherwise, the methods
 * to get the group, launch scope or other methods would use only
 * the first delegate.
 */
public class JointLauncher extends LauncherWrapper {

    private final List<ILauncher> delegates;

    public static final List<ILauncher> joinByName(final List<ILauncher> launchers) {
        final List<ILauncher> joined = new ArrayList<>();
        final Map<String, List<ILauncher>> byName = launchers.stream().collect(Collectors.groupingBy(ILauncher::getName));
        for (final List<ILauncher> launcherList: byName.values()) {
            final ILauncher launcher;
            if (launcherList.size() == 1) {
                launcher = launcherList.get(0);
            } else {
                launcher = new JointLauncher(launcherList);
            }
            joined.add(launcher);

        }
        return joined;
    }
    
    
    JointLauncher(final List<ILauncher> delegates) {
        // first delegate is the one wrapped
        super(delegates.get(0));
        this.delegates = delegates;
    }
    
    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (all)";
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Set<IParam> createParams() throws GemException {
        throw new UnsupportedOperationException(this.getClass() + " do not support joint-launchers with params");
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public List<ICommand> launch(Set<IParam> params) throws GemException {
        throw new UnsupportedOperationException(this.getClass() + " do not support joint-launchers with params");
    }

    @Override
    public List<ICommand> launch() throws GemException {
        final List<ICommand> commands = new ArrayList<>();
        for (final ILauncher launcher: delegates) {
            commands.addAll(launcher.launch());
        }
        return commands;
    }
}
