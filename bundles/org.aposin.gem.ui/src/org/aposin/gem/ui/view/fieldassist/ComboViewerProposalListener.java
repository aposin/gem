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
package org.aposin.gem.ui.view.fieldassist;

import java.util.concurrent.atomic.AtomicBoolean;
import org.aposin.gem.ui.view.fieldassist.ComboViewerProposalProvider.ObjectContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;

class ComboViewerProposalListener implements IContentProposalListener, IContentProposalListener2 {
    
    private final ComboViewerAutoCompleteField field;
    
    private final AtomicBoolean accepted = new AtomicBoolean(false);
    private ComboViewerAutoCompleteFieldHandler handler = new ComboViewerAutoCompleteFieldHandler();
    
    public ComboViewerProposalListener(final ComboViewerAutoCompleteField field) {
        this.field = field;
    }
    
    public void setHandler(final ComboViewerAutoCompleteFieldHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public void proposalAccepted(final IContentProposal proposal) {
        if (proposal != ComboViewerProposalProvider.NONE_MATCHING) {
            // first set to propagate properly to popup-close
            accepted.set(true);
            final ObjectContentProposal objProposal = (ObjectContentProposal) proposal;
            field.setSelection(objProposal.getObject());
        }
    }
    
    @Override
    public void proposalPopupOpened(final ContentProposalAdapter adapter) {
        // set to false everytime that it is open to avoid resetting
        accepted.set(false);
    }
    
    @Override
    public void proposalPopupClosed(final ContentProposalAdapter adapter) {
        // execute with some delay the reset action to allow the proposal to be set
        field.viewer.getCombo().getDisplay().timerExec(10, () -> {
            // if was not accepted (and set to false afterwards)
            if (!accepted.getAndSet(false)) {
                handler.handleNoProposalAccepted(field);
            }
        });
    }

}
