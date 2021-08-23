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
package org.aposin.gem.core.impl.internal.workflow.command;

import java.util.concurrent.CompletableFuture;
import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.impl.internal.workflow.command.base.AbstractCommand;

/**
 * No-Op command that always return a successful result
 * without running anything.
 */
public class NoOpCommand extends AbstractCommand {

    private final String details;
    
    /**
     * Constructor without details.
     * 
     * @param commandScope
     */
    public NoOpCommand(final INamedObject commandScope) {
        this(commandScope, "");
    }
    
    /**
     * Constructor with custom details.
     * 
     * @param commandScope
     * @param details non-null string with details to be logged to stdout; emty string to log nothing.
     */
    public NoOpCommand(final INamedObject commandScope, final String details) {
        super(commandScope);
        this.details = details;
    }

    @Override
    public String getDescription() {
        return details.isEmpty() ? "Nothing to run" : details;
    }

    @Override
    protected CompletableFuture<IResult> doExecute() {
        if (printCmdDesc && !details.isEmpty()) {
            getStdOut().println(details);
        }
        return CompletableFuture.completedFuture(new ResultBuilder(this).build());
    }

}
