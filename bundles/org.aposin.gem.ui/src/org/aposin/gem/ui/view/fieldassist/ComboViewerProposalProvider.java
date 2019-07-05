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
package org.aposin.gem.ui.view.fieldassist;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * Internal class to provide content for a combo viewer model.
 */
class ComboViewerProposalProvider implements IContentProposalProvider {

    static final IContentProposal NONE_MATCHING = new ContentProposal("No entry found");
    
    private final ComboViewer viewer;
    private List<ObjectContentProposal> proposals;
    
    public ComboViewerProposalProvider(final ComboViewer viewer, final List<?> proposals) {
        this(viewer);
        setProposals(proposals);
    }
    
    public ComboViewerProposalProvider(final ComboViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public IContentProposal[] getProposals(final String contents, final int position) {
        // case-insensitive content proposal (always to lower-case)
        final String lowerCaseContents = contents.toLowerCase();
        final IContentProposal[] contentProposals = proposals.stream()//
                .filter(prop -> prop.getContent().toLowerCase().contains(lowerCaseContents))//
                .toArray(IContentProposal[]::new);
        
        return contentProposals.length == 0 // 
                ? new IContentProposal[] { NONE_MATCHING } //
                : contentProposals;
    }
    
    public void setProposals(final List<?> proposals) {
        if (proposals == null || proposals.isEmpty()) {
            this.proposals = Collections.emptyList();
        } else {
            this.proposals = proposals.stream()//
                    .map(obj -> new ObjectContentProposal(obj))//
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Content proposal extension holding the viewer object.
     */
    class ObjectContentProposal extends ContentProposal {

        private final Object object;
        
        private ObjectContentProposal(final Object object) {
            super(((ILabelProvider) viewer.getLabelProvider()).getText(object));
            this.object = object;
        }
        
        public Object getObject() {
            return object;
        }

        @Override
        public String toString() {
            // for debugging purposes
            return this.getClass().getName() +  ":" + Objects.toString(object);
        }
    }

}
