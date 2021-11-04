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
package org.aposin.gem.ui.handler;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.ui.BundleProperties;
import org.aposin.gem.ui.dialog.progress.CommandProgressDialog;
import org.aposin.gem.ui.dialog.progress.internal.ObsoleteEnvironmentDialog;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for clean obsolete environment
 */
public class CleanObsoleteEnvironmentsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanObsoleteEnvironmentsHandler.class);

    @Inject
    @Translation
    private static BundleProperties bundleProperties;

    @Execute
    private void execute(final Session session, final Shell shell) {
        final List<IProject> obsoleteEnvironments = new ArrayList<>();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        Messages messages = Session.messages;
        try {
            dialog.run(true, false, monitor -> {
                monitor.beginTask(
                        messages.cleanObsoleteEnvironmentsHandler_message_fetchWorktreesProgressMonitor,
                        IProgressMonitor.UNKNOWN);
                for (final IProject project : session.getConfiguration().getProjects()) {
                    LOGGER.info(
                            String.format("Searching for obsolete environments in project : %s", project.getName()));
                    if (!project.getObsoleteEnvironments().isEmpty()) {
                        obsoleteEnvironments.add(project);
                    }
                }
                monitor.done();
            });
        } catch (InvocationTargetException | InterruptedException e) {
            LOGGER.error("Some error occurred while fetching obsolete worktrees", e);
            throw new GemException("Error while fetching obsolete worktrees", e);
        }
        if (obsoleteEnvironments.isEmpty()) {
            MessageDialog.openInformation(shell, bundleProperties.menuCleanObsoleteenvironments_label,
                    messages.cleanObsoleteEnvironmentsHandler_message_noWorktreesAvailableDialog);
        } else {
            List<IEnvironment> selectedItems = ObsoleteEnvironmentDialog.open(shell, obsoleteEnvironments);
            if (selectedItems == null) {
                return;
            } else {
                final String selectionStringMessageFormat = selectedItems.stream() //
                        .map(env -> "\t- " + env.getDisplayName()) //
                        .collect(Collectors.joining("\n"));
                boolean openConfirm = MessageDialog.openConfirm(shell,
                        bundleProperties.menuCleanObsoleteenvironments_label,
                        MessageFormat.format(messages.cleanObsoleteEnvironmentsHandler_message_confirmDeleteDialog,
                                selectionStringMessageFormat));
                if (openConfirm) {
                    final List<ICommand> cmds = new ArrayList<ICommand>();
                    for (IEnvironment selectedItem : selectedItems) {
                        ILauncher removeWorktree = selectedItem.getWorkflow().getRemoveWorktreeLauncher();
                        if (removeWorktree.canLaunch()) {
                            cmds.addAll(removeWorktree.launch());
                        } else {
                            // TODO: [#16] - should inform end-user
                            LOGGER.warn("Cannot launch worktree removal");
                        }
                    }
                    if (!cmds.isEmpty()) {
                        CommandProgressDialog.open(shell, bundleProperties.menuCleanObsoleteenvironments_label,
                                messages, cmds);
                    }
                }
            }
        }
    }
}