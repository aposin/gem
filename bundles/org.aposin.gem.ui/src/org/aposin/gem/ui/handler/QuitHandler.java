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
import javax.inject.Inject;
import org.aposin.gem.ui.BundleProperties;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Handler to quit the application with a confirmation pop-up.
 */
public final class QuitHandler {

    @Inject
    @Translation
    private static Messages messages;

    @Inject
    @Translation
    private static BundleProperties bundleProperties;

    @Execute
    public void execute(final IWorkbench workbench, final Shell shell) {
        if (confirmClose(shell)) {
            workbench.close();
        }
    }

    public static final boolean confirmClose(final Shell shell) {
        return MessageDialog.openConfirm(shell, //
                MessageFormat.format(messages.quitHandler_titleFormat_dialog,
                        bundleProperties.application_name_short), //
                messages.quitHandler_message_dialog);
    }
}
