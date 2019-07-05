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

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractCommand;

/**
 * Command wrapper to retry a command if it fails until a predicate stops it.
 * </br>
 * When the command is executed, if it returns where {@link IResult#isFailed()}
 * returns {@code false}, retries the execution until {@link #retryStopper} returns
 * {@code true}.
 */
public class RetryCommand extends AbstractCommand {

    private final ICommand command;
    private final Predicate<IResult> retryStopper;

    /**
     * Constructor for the instance.
     * 
     * @param command command to run.
     * @param retryStopper predicate testing if the command should stop the
     *                     retry process (returning {@code true}) or not
     *                     {returning @link false}.
     */
    public RetryCommand(final ICommand command, final Predicate<IResult> retryStopper) {
        super(command.getCommandScope());
        this.command = command;
        this.retryStopper = retryStopper;
    }

    @Override
    public void setPrintDescriptionBeforeRunning(boolean printCmdDesc) {
        super.setPrintDescriptionBeforeRunning(printCmdDesc);
        command.setPrintDescriptionBeforeRunning(printCmdDesc);
    }

    @Override
    public void addStdOutStream(OutputStream stdOut) {
        super.addStdOutStream(stdOut);
        command.addStdOutStream(stdOut);
    }

    @Override
    public void addStdErrStream(OutputStream stdErr) {
        super.addStdErrStream(stdErr);
        command.addStdErrStream(stdErr);
    }

    @Override
    public String getDescription() {
        return command.getDescription() + " (retry)";
    }

    @Override
    protected CompletableFuture<IResult> doExecute() {
        return command.execute().thenCompose(result -> {
            if (result.isFailed() && !retryStopper.test(result)) {
                return doExecute();
            }
            return CompletableFuture.completedFuture(result);
        });
    }

}
