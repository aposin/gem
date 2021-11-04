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
package org.aposin.gem.core.impl.internal.workflow.command.base;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.core.impl.internal.workflow.command.ResultBuilder;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractComposeCommand.AndCommand;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractComposeCommand.OrCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract command with most common functionality to implement
 * other kind of commands.
 */
public abstract class AbstractCommand implements ICommand {

    /**
     * Logger for the command.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final INamedObject commandScope;
    private final MultiOutputStream stdOutStreams = new MultiOutputStream();
    private final MultiOutputStream stdErrStreams = new MultiOutputStream();

    private boolean isStarted = false;
    private PrintStream stdOut;
    private PrintStream stdErr;
    // default transformer is to unwrap any CompletionException
    private Function<Throwable, Throwable> exceptionTransformer = t -> {
        if (t instanceof CompletionException) {
            final Throwable cause = t.getCause();
            if (cause != null) {
                return cause;
            }
        }
        return t;
    };

    /**
     * Boolean representing if the cmd-description should be printed.
     */
    protected boolean printCmdDesc = false;

    /**
     * Default constructor.
     * 
     * @param commandScope scope for the command.
     */
    protected AbstractCommand(final INamedObject commandScope) {
        this.commandScope = commandScope;
    }

    /**
     * Adds an exception transformer on top of defaults.
     * </br>
     * This will be used to wrap/unwrap any exception when constructing
     * a failed result.
     * 
     * @param exceptionTransformer transformer.
     */
    public void addExceptionTransformer(final UnaryOperator<Throwable> exceptionTransformer) {
        this.exceptionTransformer = this.exceptionTransformer.andThen(exceptionTransformer);
    }

    /**
     * {@inheritDoc}
     * </br>
     * If overriden, super should be called.
     */
    @Override
    public void addStdOutStream(final OutputStream stdOut) {
        stdOutStreams.add(stdOut);
    }

    /**
     * {@inheritDoc}
     * </br>
     * If overriden, super should be called.
     */
    @Override
    public void addStdErrStream(final OutputStream stdErr) {
        stdErrStreams.add(stdErr);
    }

    /**
     * {@inheritDoc}
     * </br>
     * If overriden, super should be called.
     */
    @Override
    public void setPrintDescriptionBeforeRunning(boolean printCmdDesc) {
        this.printCmdDesc = printCmdDesc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final INamedObject getCommandScope() {
        return commandScope;
    }

    /**
     * Execute the command.
     * </br>
     * This methods creates the std-streams (out/err) and tracks the started
     * stated (it can only be executed once at a time).
     * </br>
     * The method performs the {@link #doExecute()} and adds a handler to always
     * return a {@link #getFailedResult(Throwable)} if an exception occurs
     * and set back the started flag.
     */
    @Override
    public final CompletableFuture<IResult> execute() throws GemException {
        if (isStarted) {
            throw new GemException("Cannot execute as it is already running.");
        }
        isStarted = true;
        stdOut = new PrintStream(stdOutStreams);
        stdErr = new PrintStream(stdErrStreams);
        return doExecute().handleAsync((result, throwable) -> {
            isStarted = false;
            cleanAfterExecute();
            if (throwable != null) {
                return getFailedResult(throwable);
            }
            return result;
        });
    }

    /**
     * Do the actual execution of the command.
     * </br>
     * Called by {@link #execute()}.
     * 
     * @return 
     */
    protected abstract CompletableFuture<IResult> doExecute();

    /**
     * Do required cleanup after execution have finished (with or without errors).
     * </br>
     * Called by {@linkplain #execute()} before returning the result of {@code #doExecute()}.
     */
    protected void cleanAfterExecute() {
        // NO-OP
    }

    /**
     * Gets the result builder to construct both {@code getFailedResult} methods.
     * </br>
     * Can be overriden for custom implementations to include extra information.
     * 
     * @param errorMsg error message.
     * @return result builder for failures.
     */
    protected ResultBuilder getFailedResultBuilder(final String errorMsg) {
        return new ResultBuilder(this)//
                .withFailure(() -> true)//
                .withErrorMessage(() -> MessageFormat.format("''{0}'' command failed: {1}",
                        getDescription(), errorMsg));
    }

    /**
     * {@inheritDoc}
     * </br>
     * Impleemntation just builds the {@link #getFailedResult(String)}.
     */
    @Override
    public final IResult getFailedResult(String errorMsg) {
        return getFailedResultBuilder(errorMsg) //
                .build();
    }

    /**
     * {@inheritDoc}
     * </br>
     * Implementation just builds the {@link #getFailedResult(String)} with the
     * exception transformed by any registered {@link #addExceptionTransformer(UnaryOperator)}.
     * </br>
     * For the messages, implementors could modify based on the exception with {@link #getFailedResultMessageForException(String)}
     */
    @Override
    public final IResult getFailedResult(final Throwable exception) {
        final Throwable transformed = exceptionTransformer.apply(exception);
        return getFailedResultBuilder(getFailedResultMessageForException(transformed)) //
                .withException(transformed) //
                .build();
    }

    protected String getFailedResultMessageForException(final Throwable transformedException) {
        return transformedException.getLocalizedMessage();
    }

    /**
     * Gets the standard-out stream.
     * </br>
     * This is only initialized once {@link #execute()} is called.
     * 
     * @return print-stream.
     */
    protected final PrintStream getStdOut() {
        return stdOut;
    }

    /**
     * Gets the standard-error stream.
     * </br>
     * This is only initialized once {@link #execute()} is called.
     * 
     * @return print-stream.
     */
    protected final PrintStream getStdErr() {
        return stdErr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommand or(final INamedObject commandScope, final ICommand cmd) {
        return new OrCommand(commandScope, this, cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICommand and(final INamedObject commandScope, final ICommand cmd) {
        return new AndCommand(commandScope, this, cmd);
    }

}
