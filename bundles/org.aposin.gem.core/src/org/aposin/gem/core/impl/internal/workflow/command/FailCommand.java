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

import java.util.concurrent.CompletableFuture;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractCommand;

/**
 * Command which returns a failed result with the description.
 * </br>
 * This command could be used to fail always after another command is
 * executed (e.g., with {@link ICommand#and(ICommand)}) or to fail with
 * a better result description ({@link ICommand#or(ICommand)}.
 */
public final class FailCommand extends AbstractCommand {

    private String description;

    public FailCommand(final INamedObject commandScope, final String description) {
        super(commandScope);
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    protected CompletableFuture<IResult> doExecute() {
        getStdErr().println(description);
        return CompletableFuture.completedFuture(ResultBuilder.buildFailed(this, description));
    }


}
