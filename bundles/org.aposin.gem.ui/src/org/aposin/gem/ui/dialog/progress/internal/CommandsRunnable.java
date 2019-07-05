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
package org.aposin.gem.ui.dialog.progress.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.ICommand.IResult;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

// TODO - implement a cancel public method
public final class CommandsRunnable implements IRunnableWithProgress {

    private final String taskName;
    private final Messages messages;
    private final List<ICommand> commands;
    private final Consumer<IResult> onDoneHook;

    public CommandsRunnable(final String taskName, final Messages messages, final List<ICommand> commands,
            final Consumer<IResult> onDoneHook) {
        this.taskName = taskName;
        this.messages = messages;
        this.commands = commands;
        this.onDoneHook = onDoneHook;
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws AnyCommandFailedException {
        monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
        final Map<ICommand, Future<IResult>> futuresMap = startCommandsInParallel(commands);
        final List<IResult> results = new ArrayList<>(futuresMap.size());

        monitor.subTask(
                MessageFormat.format(messages.commandsRunnable_messageFormat_progressMonitorStart, futuresMap.size()));
        while (!futuresMap.isEmpty()) {
            final List<IResult> allDone = getDoneResults(futuresMap);
            boolean shouldCancel = monitor.isCanceled();
            // first handle the done processes
            if (!allDone.isEmpty()) {
                monitor.worked(allDone.size());
                // cleanup the future map and add to the results
                for (final IResult done : allDone) {
                    futuresMap.remove(done.getCommand());
                    addToResultList(done, results, onDoneHook);
                    // early termination if any is failing too
                    shouldCancel |= done.isFailed();
                }
                monitor.subTask(MessageFormat.format(messages.commandsRunnable_messageFormat_progressMonitorUpdate,
                        futuresMap.size(), results.size()));
            }

            // handle cancellation
            if (shouldCancel) {
                // cancell all and
                cancelAll(futuresMap.values());
                futuresMap.entrySet().stream() //
                        .map(this::getResultOrWrapIfException) //
                        .forEach(res -> addToResultList(res, results, onDoneHook));
                futuresMap.clear();
            }
        }

        if (results.stream().anyMatch(IResult::isFailed)) {
            throw new AnyCommandFailedException(results);
        }

    }

    private static void addToResultList(final IResult result, final List<IResult> results,
            final Consumer<IResult> hook) {
        hook.accept(result);
        results.add(result);
    }

    private static Map<ICommand, Future<IResult>> startCommandsInParallel(List<ICommand> commands) {
        final Map<ICommand, Future<IResult>> commandsMap = new HashMap<>(commands.size());
        for (final ICommand cmd : commands) {
            try {
                cmd.setPrintDescriptionBeforeRunning(true);
                commandsMap.put(cmd, cmd.execute());
            } catch (final GemException e) {
                // cancel all the previous ones that have already started
                cancelAll(commandsMap.values());
                // re-throw!
                throw e;
            }
        }
        return commandsMap;
    }

    private static void cancelAll(final Collection<Future<IResult>> futures) {
        for (final Future<IResult> toCancel : futures) {
            toCancel.cancel(true);
        }
    }

    private List<IResult> getDoneResults(final Map<ICommand, Future<IResult>> futuresMap) {
        final List<IResult> doneResults = new ArrayList<>(futuresMap.size());
        for (final Map.Entry<ICommand, Future<IResult>> entry : futuresMap.entrySet()) {
            if (entry.getValue().isDone()) {
                final IResult result = getResultOrWrapIfException(entry);
                doneResults.add(result);
            }
        }
        return doneResults;
    }

    private IResult getResultOrWrapIfException(
            final Map.Entry<ICommand, Future<IResult>> commandAndFuture) {
        try {
            return commandAndFuture.getValue().get();
        } catch (final InterruptedException e) {
            return commandAndFuture.getKey().getFailedResult(e);
        } catch (final ExecutionException e) {
            // use the cause instead
            return commandAndFuture.getKey().getFailedResult(e.getCause());
        } catch (final CancellationException e) {
            return commandAndFuture.getKey().getFailedResult(messages.commandsRunnable_message_cancelled);
        }
    }

    public final class AnyCommandFailedException extends GemException {
        private static final long serialVersionUID = 1L;

        private final List<IResult> results;

        private AnyCommandFailedException(final List<IResult> results) {
            super(null);
            this.results = Collections.unmodifiableList(results);
        }

        public List<IResult> getResults() {
            return results;
        }

        @Override
        public String getMessage() {
            return results.stream()//
                    .map(r -> r.isFailed() ? r.getErrorMessage() : messages.commandsRunnable_message_noFailure) //
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }

}
