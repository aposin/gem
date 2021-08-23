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
package org.aposin.gem.ui.part.listener;

import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.swt.events.SelectionListener;

/**
 * {@link SelectionListener} when a new {@link IFeatureBranch} has been set.
 */
public final class FeatureBranchSelectionListener
        extends TypedSingleSelectionChangedListener<IFeatureBranch> {

    private final Session session;

    public FeatureBranchSelectionListener(final Session session) {
        this.session = session;
    }

    @Override
    public void selectionChanged(IFeatureBranch featureBranch) {
        session.setSessionFeatureBranch(featureBranch);
    }

}
