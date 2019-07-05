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

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractCommand;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * Command that is based on a callable subclass ({@link CallableResult}).
 */
public final class CallableCommand extends AbstractCommand {

    private final CallableResult callable;
    private final String description;

    /**
     * Creates a new callable command.
     * 
     * @param commandScope scope for the command.
     * @param description description for the command.
     * @param callable task to perform.
     */
    public CallableCommand(final INamedObject commandScope, final String description,
            final CallableResult callable) {
        super(commandScope);
        this.description = description;
        this.callable = callable;
        addStdOutStream(Slf4jStream.of(logger).asInfo());
        addStdErrStream(Slf4jStream.of(logger).asError());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<IResult> doExecute() {
        // set the cmd to the callable to this
        callable.cmd = this;

        // start the process
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (printCmdDesc) {
                    callable.getStdOut().println("> Running " + description);
                }
                return callable.call();
            } catch (final Exception e) {
                return getFailedResult(e.getMessage());
            }
        });
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Callable sub-class which allows to access the {@link CallableCommand}
     * that was called and access the standard-error and standard-output
     * from it.
     */
    public abstract static class CallableResult implements Callable<IResult> {

        private CallableCommand cmd = null;

        /**
         * Gets the standard output for the {@link Callable} to log.
         * 
         * @return std-out
         */
        protected final PrintStream getStdOut() {
            return getCommand().getStdOut();
        }

        /**
         * Gets the standard error for the {@link Callable} to log.
         * 
         * @return std-err
         */
        protected final PrintStream getStdErr() {
            return getCommand().getStdErr();
        }

        /**
         * Gets the command context.
         * 
         * @return
         */
        protected final CallableCommand getCommand() {
            if (cmd == null) {
                throw new IllegalStateException("Command not set");
            }
            return cmd;
        }
    }

    /**
     * Returns the string representation for debugging purposes.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", this.getClass().getSimpleName(), description);
    }

}
