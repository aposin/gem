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
package org.aposin.gem.ui.part.listener;

import java.util.List;

import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.ui.dialog.progress.CommandProgressDialog;
import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public abstract class WorkflowRunOnSelectionListener extends SelectionAdapter {

    public abstract String getStepInformation();

    // TODO - should consider to change is run (now everytime it does)
    public abstract void onWorkflowFinished(final boolean isRun);

    public abstract List<ICommand> getWorkflowCommands();

    @Override
    public final void widgetSelected(final SelectionEvent event) {
        final List<ICommand> commands = getWorkflowCommands();
        if (!commands.isEmpty()) {
            try {
                CommandProgressDialog.open(null, getStepInformation(), Session.messages, commands);
            } finally {
                onWorkflowFinished(true);
            }
        }
    }

}
