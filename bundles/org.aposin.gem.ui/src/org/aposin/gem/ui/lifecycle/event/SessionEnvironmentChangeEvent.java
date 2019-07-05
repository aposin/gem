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
package org.aposin.gem.ui.lifecycle.event;

import java.util.Objects;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.ui.lifecycle.Session;

/**
 * Event for changes in the session's environment.
 * </br>
 * Triggered by default on {@link Session#setSessionEnvironment(IEnvironment)}.
 */
public final class SessionEnvironmentChangeEvent {

    /**
     * Topic for the event.
     */
    public static final String TOPIC = "org/aposin/gem/core/ui/event/SESSION_ENVIRONMENT_CHANGE";

    /**
     * Previous environment.
     */
    public final IEnvironment previousEnvironemnt;

    /**
     * New environment:
     */
    public final IEnvironment newEnvironment;

    /**
     * Constructor. 
     * 
     * @param previousEnvironemnt
     * @param newEnvironment
     */
    public SessionEnvironmentChangeEvent(final IEnvironment previousEnvironemnt,
            final IEnvironment newEnvironment) {
        this.previousEnvironemnt = previousEnvironemnt;
        this.newEnvironment = newEnvironment;
    }

    /**
     * Constructor for an event that only updated the environment.
     * 
     * @param updatedEnvironment
     */
    public SessionEnvironmentChangeEvent(final IEnvironment updatedEnvironment) {
        this.previousEnvironemnt = updatedEnvironment;
        this.newEnvironment = updatedEnvironment;
    }

    /**
     * Checks if the environment changed to a different one.
     * 
     * @return {@code true} if the change was to a different environment; {@code false} otherwise.
     */
    public boolean isDifferent() {
        return !Objects.equals(previousEnvironemnt, newEnvironment);
    }

}
