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
package org.aposin.gem.ui.lifecycle.event;

import java.util.Objects;

import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.lifecycle.Session;

/**
 * Event for changes in the session's feature branch.
 * </br>
 * Triggered by default on {@link Session#setSessionFeatureBranch(IFeatureBranch)}.
 */
public class SessionFeatureBranchChangeEvent {

    /**
     * Topic for the event.
     */
    public static final String TOPIC = "org/aposin/gem/core/ui/event/SESSION_FEATURE_BRANCH_CHANGE";

    /**
     * Previous feature branch.
     */
    public final IFeatureBranch previousFeatureBranch;

    /**
     * New feature branch:
     */
    public final IFeatureBranch newFeatureBranch;

    /**
     * Constructor.
     * 
     * @param previousFeatureBranch
     * @param newFeatureBranch
     */
    public SessionFeatureBranchChangeEvent(final IFeatureBranch previousFeatureBranch,
            final IFeatureBranch newFeatureBranch) {
        this.previousFeatureBranch = previousFeatureBranch;
        this.newFeatureBranch = newFeatureBranch;
    }

    /**
     * Checks if the feature-branch changed to a different one.
     * 
     * @return {@code true} if the change was to a different feature-branch; {@code false} otherwise.
     */
    public boolean isDifferent() {
        return !Objects.equals(previousFeatureBranch, newFeatureBranch);
    }

}
