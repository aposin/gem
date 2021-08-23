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
import java.util.concurrent.CompletableFuture;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.impl.internal.workflow.command.ResultBuilder;

/**
 * Helper class for compose commands.
 * </br>
 * Only to be used in {@link AbstractCommand}.
 */
abstract class AbstractComposeCommand extends AbstractCommand {

    private final String composeOperator;
    private final ICommand first;
    private final ICommand second;

    /**
     * Constructor for the class.
     * 
     * @param commandScope scope for the command.
     * @param composeOperator string representing the composition operation.
     * @param first left-side of the composed command.
     * @param second second-side of the composed command.
     */
    private AbstractComposeCommand(final INamedObject commandScope, final String composeOperator,
            final ICommand first, final ICommand second) {
        super(commandScope);
        this.composeOperator = composeOperator;
        this.first = first;
        this.second = second;
    }

    @Override
    public final void addStdOutStream(final OutputStream stdOut) {
        super.addStdOutStream(stdOut);
        first.addStdOutStream(stdOut);
        second.addStdOutStream(stdOut);
    }

    @Override
    public final void addStdErrStream(final OutputStream stdErr) {
        super.addStdErrStream(stdErr);
        first.addStdErrStream(stdErr);
        second.addStdErrStream(stdErr);
    }

    @Override
    public final void setPrintDescriptionBeforeRunning(boolean printCmdDesc) {
        super.setPrintDescriptionBeforeRunning(printCmdDesc);
        first.setPrintDescriptionBeforeRunning(printCmdDesc);
        second.setPrintDescriptionBeforeRunning(printCmdDesc);
    }

    @Override
    protected final CompletableFuture<IResult> doExecute() {
        return doExecuteComposition(first, second);
    }

    @Override
    public final String getDescription() {
        return String.format("(%s %s %s)", first.getDescription(), composeOperator,
                second.getDescription());
    }

    /**
     * Execute the composition.
     * 
     * @param first left-side of the composed command.
     * @param second right-side of the composed command.
     * @return the execution of the composed command.
     */
    protected abstract CompletableFuture<IResult> doExecuteComposition(final ICommand first,
            final ICommand second);

    /**
     * Command implementation for AND.
     */
    static class AndCommand extends AbstractComposeCommand {


        public AndCommand(final INamedObject commandScope, final ICommand first,
                final ICommand second) {
            super(commandScope, "AND", first, second);
        }

        @Override
        protected CompletableFuture<IResult> doExecuteComposition(final ICommand first,
                final ICommand second) {
            return first.execute()//
                    .thenCompose(r -> {
                        if (r.isFailed()) {
                            logger.trace("Not running AND command: {}", second.getDescription());
                            return CompletableFuture.completedFuture(r);
                        } else {
                            logger.trace("Running AND command: {}", second.getDescription());
                            return second.execute();
                        }
                    }) //
                    .thenApply(result -> ResultBuilder.fromResult(this, result).build());
        }
    }

    /**
     * Command implementation for OR.
     */
    static class OrCommand extends AbstractComposeCommand {


        public OrCommand(final INamedObject commandScope, final ICommand first,
                final ICommand second) {
            super(commandScope, "OR", first, second);
        }

        @Override
        protected CompletableFuture<IResult> doExecuteComposition(final ICommand first,
                final ICommand second) {
            return first.execute()//
                    .thenCompose(r -> {
                        if (r.isFailed()) {
                            logger.trace("Running OR command: {}", second.getDescription());
                            return second.execute();
                        } else {
                            logger.trace("Not running OR command: {}", second.getDescription());
                            return CompletableFuture.completedFuture(r);
                        }
                    });
        }
    }
}
