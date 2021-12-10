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
package org.aposin.gem.ui.dialog.obsolete;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.ui.message.Messages;
import org.aposin.gem.ui.view.labelprovider.TypedColumnLabelProvider.TypedColumnLabelProviderFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Dialog for obsolete environment
 */
public class ObsoleteEnvironmentDialog extends Dialog {

    private final List<IProject> obsoleteEnvironments;
    private final int itemSize;
    private final String title;
    private final Messages messages;
    
    private ObsoleteEnvironmentsView view;

    private Object[] selectedItems;

    /**
     * Constructor
     * @param parent
     * @param obsoleteEnvironments
     */
    private ObsoleteEnvironmentDialog(final Shell parent, String title, final Messages messages,
            final List<IProject> obsoleteEnvironments) {
        super(parent);
        this.title = title;
        this.messages = messages;
        this.obsoleteEnvironments = obsoleteEnvironments;
        this.itemSize = obsoleteEnvironments.size()
                + obsoleteEnvironments.stream().flatMap(p -> p.getObsoleteEnvironments().stream()).toArray().length;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite tableComposite = (Composite) super.createDialogArea(parent);
        tableComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL));
        // create the view and set layout and text/providers
        view = new ObsoleteEnvironmentsView(tableComposite, GridData.FILL);
        view.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
        applyMessages();
        view.getCheckboxTreeViewer().setContentProvider(new TreeContentProvider());
        setColumnLabelProviders();
        GridLayoutFactory.fillDefaults().generateLayout(parent);
        applyDialogFont(tableComposite);
        // add listener(s)
        CheckboxTreeViewer checkboxTreeViewer = view.getCheckboxTreeViewer();
        checkboxTreeViewer.addCheckStateListener(event -> setCheckedState(event.getElement(), event.getChecked()));
        view.getSelectAllButton().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean checkState = ((Button) e.getSource()).getSelection();
                obsoleteEnvironments.stream().forEach(project -> setCheckedState(project, checkState));
            }
        });
        // set the input 
        checkboxTreeViewer.setInput(this.obsoleteEnvironments);
        // trigger layout with all check-boxes expanded and columns packed 
        checkboxTreeViewer.expandAll();
        TreeColumn[] columns = checkboxTreeViewer.getTree().getColumns();
        for (TreeColumn column : columns) {
            column.pack();
        }
        checkboxTreeViewer.refresh();
        return tableComposite;
    }

    private void setCheckedState(Object element, boolean checked) {
        final CheckboxTreeViewer checkboxTreeViewer = view.getCheckboxTreeViewer();
        checkboxTreeViewer.setSubtreeChecked(element, checked);
        // if child node check changed, update parent node accordingly.
        if (element instanceof IEnvironment) {
            ITreeContentProvider provider = (ITreeContentProvider) checkboxTreeViewer.getContentProvider();
            final Object parentElement = provider.getParent(element);
            final boolean parentChecked = checkboxTreeViewer.getChecked(parentElement);
            // if unchecked
            if (!checked && parentChecked) {
                final boolean noneChecked = Arrays.stream(provider.getChildren(parentElement)) //
                        .noneMatch(checkboxTreeViewer::getChecked);
                if (noneChecked) {
                    checkboxTreeViewer.setChecked(parentElement, false);
                }
            } else if (!parentChecked) { // if checked
                final boolean allChecked = Arrays.stream(provider.getChildren(parentElement)) //
                        .allMatch(checkboxTreeViewer::getChecked);
                if (allChecked) {
                    checkboxTreeViewer.setChecked(parentElement, true);
                }
            }
        }
        selectedItems = checkboxTreeViewer.getCheckedElements();
        getButton(IDialogConstants.OK_ID).setEnabled(selectedItems.length > 0);
        view.getSelectAllButton().setSelection(selectedItems.length == itemSize);
    }

    private void applyMessages() {
        view.getColumnProject().getColumn().setText(messages.project_label_common);
        view.getColumnEnvironment().getColumn().setText(messages.environment_label_common);
        view.getColumnWorktree().getColumn().setText(messages.worktree_label_common);
        view.getSelectAllButton().setText(messages.obsoleteEnvironmentDialog_label_selectAllButton);
    }

    private void setColumnLabelProviders() {
        TypedColumnLabelProviderFactory.create(INamedObject.class) //
                .ignoreUntyped(true) //
                .text(element ->
                {
                    if (element instanceof IEnvironment) {
                        return ((IEnvironment) element).getProject().getDisplayName();
                    }
                    // assume that all the rest are the IProject
                    return element.getDisplayName();
                }) //
                .set(view.getColumnProject());

        TypedColumnLabelProviderFactory.create(IEnvironment.class) //
                .ignoreUntyped(true) //
                .text(IEnvironment::getDisplayName) //
                .set(view.getColumnEnvironment());

        TypedColumnLabelProviderFactory.create(IEnvironment.class) //
                .ignoreUntyped(true) //
                .text(element -> element.getWorktreesBaseLocation().toString()) //
                .set(view.getColumnWorktree());
    }

    private class TreeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            return ArrayContentProvider.getInstance().getElements(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IProject) {
                return ((IProject) parentElement).getObsoleteEnvironments().toArray();
            }
            return null;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IEnvironment) {
                return ((IEnvironment) element).getProject();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return element instanceof IProject;
        }
    }

    /**
     * Opens dialog with obsolete environments 
     * @param parentShell
     * @param obsoleteEnvironments
     * @return the {@code List<IEnvironment>} containing selected obsolete environments. 
     * Returns empty list if no items are selected
     */
    public static List<IEnvironment> open(final Shell parentShell, final String title, final Messages messages,
                                          final List<IProject> obsoleteEnvironments) {
        final ObsoleteEnvironmentDialog dialog = new ObsoleteEnvironmentDialog(parentShell, title, messages,
                obsoleteEnvironments);
        if (dialog.open() != Window.CANCEL && dialog.selectedItems != null) {
            return Stream.of(dialog.selectedItems) //
                    .filter(IEnvironment.class::isInstance) //
                    .map(IEnvironment.class::cast) //
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}