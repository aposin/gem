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
package org.aposin.gem.ui.dialog.progress;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;

import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.ui.dialog.progress.internal.CliProgressMonitorDialog;
import org.aposin.gem.ui.dialog.progress.internal.CommandsRunnable;
import org.aposin.gem.ui.dialog.progress.internal.CommandsRunnable.AnyCommandFailedException;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class CommandProgressDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandProgressDialog.class);

    private CommandProgressDialog() {
        // cannot be instantiated - run over the open() method
    }

    /**
     * Open the dialog and run the commands.
     * 
     * @param name
     * @param commands
     * @return {@code true} if the pipeline was run; {@code false} otherwise.
     */
    public static boolean open(final Shell parent, final String name, final Messages messages,
            final List<ICommand> commands) {
        IStatus error = null;
        final CliProgressMonitorDialog dialog = new CliProgressMonitorDialog(parent, messages, commands);
        try {
            // result consumer to update the field decorator
            final Consumer<IResult> resultConsumer = r -> {
                if (r.isFailed()) {
                    dialog.setCommandFailedImage(r.getCommand());
                } else {
                    dialog.setCommandCompletedImage(r.getCommand());
                }
                LOGGER.debug("Command finished");
            };
            // never allows cancel!
            dialog.run(true, false, new CommandsRunnable(name, messages, commands, resultConsumer));
        } catch (final InvocationTargetException | InterruptedException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof AnyCommandFailedException) {
                error = handleAnyCommandFailedStatus(name, messages, (AnyCommandFailedException) cause);
            } else {
                error = handleException(cause);
            }
        } catch (final GemException e) {
            error = handleException(e);
        }

        if (error != null) {
            enableShowErrors(dialog, name, error);
            openError(dialog, name, error);
        } else {
            MessageDialog.openInformation(parent, name, messages.commandProgressDialog_message_successDialog);
        }

        // any case, it run with/without errors
        // TODO - should return instead the status of the run?
        return true;
    }


    public static final void enableShowErrors(final CliProgressMonitorDialog dialog,
            final String name, final IStatus error) {
        if (error != null) {
            dialog.getShowErrorsButton().setEnabled(true);
            dialog.getShowErrorsButton().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    openError(dialog, name, error);
                }

            });
        }
    }

    private static void openError(final CliProgressMonitorDialog dialog, final String name,
            final IStatus error) {
        ErrorDialog.openError(dialog.getShell(), name, null, error);
    }


    private static final IStatus handleException(final Throwable exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new Status(IStatus.ERROR, exception.getClass().getName(),
                exception.getLocalizedMessage(), exception);
    }

    private static final IStatus handleAnyCommandFailedStatus(final String name,
                                                              final Messages messages,
                                                              final AnyCommandFailedException anyCommandFailedException) {
        final MultiStatus multiStatus = new MultiStatus(CommandProgressDialog.class.getName(),
                IStatus.ERROR,
                MessageFormat.format(messages.commandProgressDialog_messageFormat_statusWithErrors, name), null);
        anyCommandFailedException.getResults().stream()//
                .peek(CommandProgressDialog::logResultIfFailed) //
                .map(result -> new Status(//
                        // status depending on the failure
                        result.isFailed() ? IStatus.ERROR : IStatus.OK, //
                        result.getClass().getName(), // class name for Plug-in ID (fake)
                        result.getErrorMessage(), // error message
                        result.getException())) // exception (if any)
                .forEach(multiStatus::add);

        return multiStatus;
    }
    
    private static final void logResultIfFailed(final IResult result) {
        if (result.isFailed()) {
            LOGGER.error(result.getErrorMessage(), result.getException());
        }
    }
    
}
