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
package org.aposin.gem.core.impl.internal.workflow;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.WorkflowException;
import org.aposin.gem.core.exception.GemException;

public class WorkflowLauncherBuilder {

    private final INamedObject scope;
    private final String name;
    // with setters or default
    private String displayName;
    private String exceptionMessage;
    private BooleanSupplier canLaunchSupplier;

    public WorkflowLauncherBuilder(final INamedObject scope, final String name) {
        this.scope = scope;
        this.name = name;
        this.canLaunchSupplier = () -> true;
    }

    public ILauncher build(final Supplier<List<ICommand>> cmdBuilder) {
        return new AbstractNoParamsLauncher() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return displayName != null ? displayName : name;
            }

            @Override
            public INamedObject getLaunchScope() {
                return scope;
            }

            @Override
            public INamedObject getGroup() {
                return scope;
            }

            @Override
            public boolean canLaunch() {
                return canLaunchSupplier != null ? canLaunchSupplier.getAsBoolean() : true;
            }

            @Override
            public List<ICommand> launch() throws GemException {
                if (!canLaunch()) {
                    throw new WorkflowException(
                            exceptionMessage != null ? exceptionMessage : "Cannot launch " + getDisplayName());
                }
                return cmdBuilder.get();
            }

        };
    }

    public WorkflowLauncherBuilder displayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public WorkflowLauncherBuilder canLaunch(final BooleanSupplier supplier) {
        this.canLaunchSupplier = supplier;
        return this;
    }

    public WorkflowLauncherBuilder exceptionMessage(final String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

}
