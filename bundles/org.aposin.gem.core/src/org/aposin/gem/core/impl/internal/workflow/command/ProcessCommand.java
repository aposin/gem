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
package org.aposin.gem.core.impl.internal.workflow.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.impl.internal.util.CProcessExecutor;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractCommand;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

/**
 * Command which runs a process through {@link CProcessExecutor}.
 * </br>
 * This is also a process listener, to allow hooking into the command.
 */
public class ProcessCommand extends AbstractCommand {

    private final CProcessExecutor executor;
    private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    private final ProcessListener listener = new ProcessListener() {

        /**
         * Prints in the standard-output the command on startup of the command.
         */
        @Override
        public final void beforeStart(final ProcessExecutor executor) {
            if (printCmdDesc) {
                getStdOut().println(getCommandLineHeader());
            }
        }

    };
    /**
     * Default constructor.
     * 
     * @param commandScope scope for the command.
     * @param executor custom process executor
     * (should be an instance of {@link CProcessExecutor}).
     */
    public ProcessCommand(final INamedObject commandScope, final ProcessExecutor executor) {
        super(commandScope);
        this.executor = (CProcessExecutor) executor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<IResult> doExecute() {
        try {
            final StartedProcess startedProcess = applyStreamsAndListener(executor).start();
            return ((CompletableFuture<ProcessResult>) startedProcess.getFuture()) //
                    .handleAsync((pr, ex) -> {
                        handleFinishedProcess(startedProcess);
                        if (ex != null) {
                            // this relies that the error code handling is done with the
                            // ProcessExecutor
                            return getFailedResult(ex);
                        }
                        // successful run!
                        return new ResultBuilder(this).build();
                    });
        } catch (final IOException e) {
            getStdErr().println("Error " + e.getLocalizedMessage());
            // get as a failed result
            return CompletableFuture.completedFuture(getFailedResult(e.getLocalizedMessage()));
        }
    }

    private void handleFinishedProcess(final StartedProcess startedProcess) {
        final Process process = startedProcess.getProcess();
        // if the process was cancelled but it is still alive
        if (startedProcess.getFuture().isCancelled() && process.isAlive()) {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ResultBuilder getFailedResultBuilder(final String errorMsg) {
        return super.getFailedResultBuilder(errorMsg) //
                .withErrorMessage(() -> buildErrorMessage(errorMsg)); // include the stdErr if any!
    }

    private final String buildErrorMessage(final String errorMsg) {
        return MessageFormat.format("''{0}'' command failed: {1}\n{2}",
                getDescription(), errorMsg, stdErr);
    }

    @Override
    protected String getFailedResultMessageForException(final Throwable transformedException) {
        if (transformedException instanceof InvalidExitValueException) {
            return buildErrorMessage("unexpected exit value -> "  + ((InvalidExitValueException) transformedException).getExitValue());
        }
        return transformedException.getLocalizedMessage();
    }

    /**
     * Cleans the accumulated std-error if a re-run is done.
     */
    @Override
    protected void cleanAfterExecute() {
        // reset the std-error
        stdErr.reset();
    }

    private boolean setStreamsAndListener = true;
    
    /**
     * Helper method to add the stdErr and stdOut streams to the executor.
     * </br>
     * It only adds the streams/listener if {@code setStreamsAndListener} is set.
     * 
     * @param executor
     * @return same executor with the streams applied
     */
    private final ProcessExecutor applyStreamsAndListener(final ProcessExecutor executor) {
        if (setStreamsAndListener) {
            // always redirect to the byte stream
            executor.redirectErrorAlsoTo(stdErr);
            // redirect to the listeners
            executor.redirectOutputAlsoTo(getStdOut());
            executor.redirectErrorAlsoTo(getStdErr());
            executor.addListener(listener);
            setStreamsAndListener = false;
        }
        return executor;
    }

    @Override
    public String getDescription() {
        return executor.getCommand().stream().collect(Collectors.joining(" "));
    }

    /**
     * Helper method to format the command in a cmd-like way.
     * 
     * @return formatted string.
     */
    public String getCommandLineHeader() {
        return MessageFormat.format("{0} $> {1}", //
                executor.getDirectory() == null ? "~" : executor.getDirectory(), //
                getDescription());
    }

    /**
     * Returns the string representation for debugging purposes.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", this.getClass().getSimpleName(), getCommandLineHeader());
    }
}
