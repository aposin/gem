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

import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * Custom auto-complete field based on {@link org.eclipse.jface.fieldassist.AutoCompleteField}.
 * </br>
 * The field should be used for every action (set input, selection, etc) on the {@link ComboViewer} 
 * to assess that the auto-complete functionality works as expected.
 * Methods for those actions are provided in this class.
 * </br>
 * IMPORTANT: only {@link ComboViewer} with a {@link Combo} control is supported.
 */
public class ComboViewerAutoCompleteField {
    
    /**
     * Package protected to have access on the derived classes.
     */
    /*package*/ final ComboViewer viewer;
    private final ContentProposalAdapter adapter;
    private final ComboViewerProposalProvider provider;
    private final ComboViewerProposalListener listener;
    
    /**
     * Constructor for a {@link ComboViewer}.
     *
     * @param viewer non-null viewer for a {@link Combo}.
     */
    public ComboViewerAutoCompleteField(final ComboViewer viewer) {
        this.viewer = viewer;
        this.viewer.setContentProvider(ArrayContentProvider.getInstance());
        this.provider = new ComboViewerProposalProvider(viewer);
        // configure proposal adapter
        this.adapter = new ContentProposalAdapter(viewer.getControl(), //
                new ComboContentAdapter(), //
                provider, //
                null, //
                null);
        this.adapter.setPropagateKeys(true);
        this.adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
        
        // add listeners
        // listener for the combo itself, to open the proposal not in the first click
        viewer.getCombo().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                viewer.getCombo().setListVisible(true);
            }
        });
        
        this.listener = new ComboViewerProposalListener(this);
        adapter.addContentProposalListener((IContentProposalListener) listener);
        adapter.addContentProposalListener((IContentProposalListener2) listener);
        
        // hack to close the popup when other part of the shell is clicked
        // so in every mouse down on the display, it closes the popup if open iff:
        // - it is not the viewer combo itself (already handled) and
        // - it is the same shell as the combo
        viewer.getCombo().getDisplay().addFilter(SWT.MouseDown, event -> {
                if (event.widget instanceof Control) {
                    final Control control = (Control) event.widget;
                    if (control != viewer.getCombo() && control.getShell() == viewer.getCombo().getShell()) {
                        triggerPopupClose();
                    }
                }
        });
        
    }
    
    private void triggerPopupClose() {
        if (adapter.isProposalPopupOpen()) {
            // the only way to close the popup is to disable and re-enable it
            adapter.setEnabled(false);
            adapter.setEnabled(true);
        }
    }

    /**
     * Enables or disables the field.
     * 
     * @param enabled {@code true} to enable; {@code false} otherwise.
     */
    public void setEnabled(final boolean enabled) {
        viewer.getCombo().setEnabled(enabled);
    }
    
    /**
     * Sets the list of objects for the combo.
     * 
     * @param input list of objects.
     */
    public void setInput(final List<?> input) {
        viewer.setInput(input);
        provider.setProposals(input);
    }
    
    /**
     * Sets the selected item.
     * </br>
     * Should be one of the provided by the {@link #setInput(List)}.
     * 
     * @param item selected object.
     */
    public void setSelection(final Object item) {
        viewer.setSelection(new StructuredSelection(item));
    }
    
    /**
     * Sets the label provider for the viewer.
     * 
     * @param labelProvider
     */
    public void setLabelProvider(final ILabelProvider labelProvider) {
        viewer.setLabelProvider(labelProvider);
    }
    
    /**
     * Sets the selection change listener for the viewer.
     * 
     * @param listener
     * @see #setSelection(Object)
     */
    public void addSelectionChangedListener(final ISelectionChangedListener listener) {
        viewer.addSelectionChangedListener(listener);
    }
    
    /**
     * Refresh the field.
     */
    public void refresh() {
        viewer.refresh();
    }
    
    /**
     * Clear the input of the field.
     */
    public void clearInput() {
        viewer.getCombo().removeAll();
        provider.setProposals(Collections.emptyList());
    }
    
    /**
     * Sets the handler when no proposal is accepted.
     * 
     * @param noProposalHandler
     */
    public void setHandler(final ComboViewerAutoCompleteFieldHandler handler) {
        this.listener.setHandler(handler);
    }
    
}
