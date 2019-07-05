/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur FÃ¶rderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.ui.dialog;

import java.text.MessageFormat;
import java.util.List;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.ui.message.Messages;
import org.aposin.gem.ui.part.PartHelper;
import org.aposin.gem.ui.view.DynamicButtonGroupListView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Dialog asking to inform the user that a conflict happened.
 * and give the options to proceed.
 */
public class MergeConflictDialog extends MessageDialog {

    private final String themeId;
    private final List<ILauncher> launchers;
    
    public MergeConflictDialog(final String dialogTitle, final String dialogMessage,
            final String themeId, final List<ILauncher> launchers,
            final String... dialogButtonLabels) {
        super(null, dialogTitle, null, dialogMessage, WARNING, 0, dialogButtonLabels);
        this.themeId = themeId;
        this.launchers = launchers;
    }
    
    /**
     * Open dialog to ask if conflict-resolution is done.
     * 
     * @param msgRegistry class to get the messages.
     * @return {@code true} to continue merging; {@code false} otherwise.
     */
    public static boolean openContinue(final Messages msg, final String themeId, final IWorktreeDefinition worktree) {
        final String okLabel = msg.mergeConflictDialog_label_continueButton;
        final String cancelLabel = msg.mergeConflictDialog_label_abortButton;
        final String title = MessageFormat.format(
                msg.mergeConflictDialog_titleFormat_dialog, //
                worktree.getRepository().getId());
        final String message = MessageFormat.format(
                msg.mergeConflictDialog_messageFormat_dialog, //
                worktree.getRepository().getId(), okLabel, cancelLabel);
        
        final MergeConflictDialog dialog = new MergeConflictDialog(title, message, //
                // TODO: use a new extension method for conflict launchers (retrieved from session)
                themeId, null, //
                okLabel, cancelLabel);
        dialog.setShellStyle(dialog.getShellStyle() | SWT.SHEET);
        return dialog.open() == 0;
    }
    
    @Override
    protected Control createCustomArea(final Composite parent) {
        if (launchers == null || launchers.isEmpty()) {
            // no launchers is none or empty
            return null;
        }
        // TODO: this create a group-list view, but maybe better a single group or no group?
        final DynamicButtonGroupListView view = new DynamicButtonGroupListView(parent, SWT.HORIZONTAL);
        PartHelper.recreateLauncherButtons(view, launchers, themeId);
        return view;
    }
    
}
