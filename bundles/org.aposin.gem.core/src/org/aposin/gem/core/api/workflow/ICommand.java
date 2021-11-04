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
package org.aposin.gem.core.api.workflow;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.exception.GemException;

/**
 * Defines an independent command that should be run on a concrete part of
 * a workflow.
 */
public interface ICommand {

    /**
     * Adds an output stream to write the results from the standard output.
     */
    public void addStdOutStream(final OutputStream stdOut);

    /**
     * Adds an output stream to write the results from the standard error.
     */
    public void addStdErrStream(final OutputStream stdErr);

    /**
     * Sets if the description should be printed in all {@link OutputStream}
     * registered on the {@link #addStdOutStream(OutputStream)}.
     * 
     * @param printCmdDesc {@code true} to print the command; {@code false} otherwise.
     */
    public void setPrintDescriptionBeforeRunning(final boolean printCmdDesc);

    /**
     * Gets the description for the command.
     * 
     * @return description for the command.
     */
    public String getDescription();

    /**
     * Execute the command.
     * 
     * @return command result (as future).
     */
    public CompletableFuture<IResult> execute() throws GemException;

    /**
     * Creates a failed result with an error message.
     * 
     * @param errorMsg error message
     * @return failed result.
     */
    public IResult getFailedResult(final String errorMsg);

    /**
     * Creates a failed result with a wrapped throwable.
     * 
     * @param exception throwable.
     * @return failed result.
     */
    public IResult getFailedResult(final Throwable exception);

    /**
     * Gets the object where this command acts on.
     * </br>
     * This method should be used to present an user-friendly
     * name for the command.
     * 
     * @return command scope (e.g., repository/environment/etc).
     */
    public INamedObject getCommandScope();

    /**
     * Runs the second command only if this one does not fail.
     * 
     * @param cmd command to run.
     * @return composable AND command.
     */
    public ICommand and(final INamedObject commandScope, final ICommand cmd);

    /**
     * Runs the second command only if this one does not fail.
     * 
     * @param cmd command to run.
     * @return composable AND command.
     */
    public default ICommand and(final ICommand cmd) {
        return and(this.getCommandScope(), cmd);
    }

    /**
     * Runs the second command only if this one fail.
     * 
     * @param cmd command to run.
     * @return composable OR command.
     */
    public ICommand or(final INamedObject commandScope, final ICommand cmd);

    /**
     * Runs the second command only if this one fail.
     * 
     * @param cmd command to run.
     * @return composable OR command.
     */
    public default ICommand or(final ICommand cmd) {
        return or(this.getCommandScope(), cmd);
    }

    /**
     * Represents the result of the command execution.
     */
    public static interface IResult {

        /**
         * Gets the command that generated this result.
         * 
         * @return command
         */
        public ICommand getCommand();

        /**
         * Checks if the result represents a failure.
         * 
         * @return {@code true} if the command have failed; {@code false} otherwise.
         */
        public boolean isFailed();

        /**
         * Gets the error message if the result have failed.
         * 
         * @return error message; {@code null} if not failed.
         */
        public String getErrorMessage();

        /**
         * Gets the wrapped exception (if any) if the result have failed.
         * 
         * @return exception; {@code null} if not failed or available.
         */
        public Throwable getException();

    }

}
