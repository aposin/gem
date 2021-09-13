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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.ui.dialog.progress.CommandProgressDialog;
import org.aposin.gem.ui.dialog.progress.internal.ObsoleteEnvironmentDialog;
import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for clean obsolete environment.
 */
public class CleanObsoleteEnvironmentsHandler extends GemAbstractSessionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanObsoleteEnvironmentsHandler.class);

    @Override
    public void doExecute(final Session session) throws Exception {
        final List<IProject> obsoleteEnvironments = loadObsoleteWorkspacesWithProgressDialog(session);
        if (obsoleteEnvironments.isEmpty()) {
            MessageDialog.openInformation(null, session.bundleProperties.menuCleanObsoleteenvironments_label,
                    session.messages.cleanObsoleteEnvironmentsHandler_message_noWorktreesAvailableDialog);
        } else {
            List<IEnvironment> selectedItems = ObsoleteEnvironmentDialog.open(null, obsoleteEnvironments);
            if (confirmDeletion(session, selectedItems)) {
                // all the commands should be able to launch (otherwise the obsolete algorithm is wrong)
                // so canLaunch should be true for all
                final List<ICommand> cmds = selectedItems.stream() //
                        .flatMap(env -> env.getWorkflow().getRemoveWorktreeLauncher().launch().stream()) //
                        .collect(Collectors.toList());
                CommandProgressDialog.open(null, //
                        session.bundleProperties.menuCleanObsoleteenvironments_label, //
                        session.messages, //
                        cmds);
            }
        }
    }

    private List<IProject> loadObsoleteWorkspacesWithProgressDialog(final Session session) throws Exception {
        final List<IProject> obsoleteEnvironments = new ArrayList<>();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        dialog.run(true, false, monitor -> {
            monitor.beginTask(session.messages.cleanObsoleteEnvironmentsHandler_message_fetchWorktreesProgressMonitor,
                    IProgressMonitor.UNKNOWN);
            for (final IProject project : session.getConfiguration().getProjects()) {
                LOGGER.debug("Searching for obsolete environments in project: {}", project.getName());
                if (!project.getObsoleteEnvironments().isEmpty()) {
                    obsoleteEnvironments.add(project);
                }
            }
            monitor.done();
        });
        return obsoleteEnvironments;
    }

    private boolean confirmDeletion(final Session session, List<IEnvironment> obsoleteEnvironments) {
        if (!obsoleteEnvironments.isEmpty()) {
            final String selectionStringMessageFormat = obsoleteEnvironments.stream() //
                    .map(this::formatEnvironmentList) //
                    .collect(Collectors.joining("\n"));
            return MessageDialog.openConfirm(null, //
                    session.bundleProperties.menuCleanObsoleteenvironments_label,
                    MessageFormat.format(
                            session.messages.cleanObsoleteEnvironmentsHandler_message_confirmDeleteDialog,
                            selectionStringMessageFormat));
        }
        return false;
    }

    private String formatEnvironmentList(final IEnvironment env) {
        return MessageFormat.format("\t- {0} / {1}", env.getProject().getDisplayName(), env.getDisplayName());
    }

}