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
package org.aposin.gem.ui.part.listener;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.aposin.gem.core.api.IRefreshable;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.ui.dialog.ParamsInputDialog;
import org.aposin.gem.ui.dialog.progress.CommandProgressDialog;
import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Listener that on selection runs the provided launcher.
 * </br>
 * Implements {@link IRefreshable} reset properly sub-classes.
 */
public class LauncherSelectionListener extends SelectionAdapter implements IRefreshable {

    /**
     * Launcher to be used.
     */
    protected ILauncher launcher;

    /**
     * Constructor.
     * 
     * @param launcher
     */
    public LauncherSelectionListener(final ILauncher launcher) {
        this.launcher = launcher;
    }

    /**
     * Gets the launcher to be used.
     * </br>
     * Might be overriden to provide custom behavior.
     * 
     * @return the launcher.
     */
    public ILauncher getLauncher() {
        return launcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void widgetSelected(final SelectionEvent e) {
        final ILauncher currentLauncher = getLauncher();
        if (currentLauncher == null || !currentLauncher.canLaunch()) {
            MessageDialog.openWarning(null,
                    currentLauncher == null ? null : currentLauncher.getDisplayName(),
                    "Cannot launch!");
            return;
        }

        final Supplier<List<ICommand>> launcherMethod;
        if (currentLauncher.requireParams()) {
            final ParamsInputDialog dialog = new ParamsInputDialog(null,
                    currentLauncher.getDisplayName(), launcher.createParams());
            dialog.open();
            if (dialog.getParams() == null) {
                // do not launch (as it was canceled by the user or input was not valid)
                launcherMethod = Collections::emptyList;
            } else {
                // TODO - validate args before? -> should be done at dialog level?
                launcherMethod = () -> currentLauncher.launch(dialog.getParams());
            }
        } else {
            launcherMethod = currentLauncher::launch;
        }

        try {
            final List<ICommand> cmds = launcherMethod.get();
            // finally, if there is some command that should be run with progress
            // launch it
            if (!cmds.isEmpty()) {
                CommandProgressDialog.open(null, getLauncher().getDisplayName(), //
                        Session.messages, cmds);
            }
        } catch (final GemException exc) {
            MessageDialog.openError(null, "Launch error",
                    MessageFormat.format("Error launching ''{0}'':\n\n{1}",
                            getLauncher().getDisplayName(), //
                            exc.getLocalizedMessage()));
        }
    }

    @Override
    public void refresh() {
        // NO- OP for this class
    }

}
