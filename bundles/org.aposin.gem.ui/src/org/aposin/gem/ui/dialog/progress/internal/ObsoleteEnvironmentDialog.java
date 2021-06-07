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
package org.aposin.gem.ui.dialog.progress.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.message.Messages;
import org.aposin.gem.ui.view.ObsoleteEnvironmentsView;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Dialog for obsolete environment
 */
public class ObsoleteEnvironmentDialog extends Dialog {

    private final List<IProject> obsoleteEnvironments;
    private Object[] selectedItems;
    private ObsoleteEnvironmentsView view;


    /**
     * Constructor
     * @param parent
     * @param obsoleteEnvironments
     */
    private ObsoleteEnvironmentDialog(final Shell parent, final List<IProject> obsoleteEnvironments) {
        super(parent);
        this.obsoleteEnvironments = obsoleteEnvironments;

    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Session.messages.cleanObsoleteEnvironmentsHandler_title_commandProgressDialog);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite tableComposite = (Composite) super.createDialogArea(parent);
        tableComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL));

        view = new ObsoleteEnvironmentsView(tableComposite, GridData.FILL);
        view.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
        Messages messages = Session.messages;
        view.getColumnProject().getColumn().setText(messages.project_label_common);
        view.getColumnEnvironment().getColumn().setText(messages.environment_label_common);
        view.getColumnWorktree().getColumn().setText(messages.worktree_label);
        view.getColumnBranch().getColumn().setText(messages.branch_label);
        view.getCheckboxTreeViewer().setContentProvider(new TreeContentProvider());
        CheckboxTreeViewer checkboxTreeViewer = view.getCheckboxTreeViewer();
        checkboxTreeViewer.addCheckStateListener(event -> {
            checkboxTreeViewer.setSubtreeChecked(event.getElement(), event.getChecked());
            selectedItems = checkboxTreeViewer.getCheckedElements();

        });

        view.getColumnProject().setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IEnvironment) {
                    return ((IEnvironment) element).getProject().getDisplayName();
                } else if (element instanceof IProject) {
                    return ((IProject) element).getDisplayName();
                }
                return super.getText(element);
            }
        });

        view.getColumnEnvironment().setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IEnvironment) {
                    return ((IEnvironment) element).getDisplayName();
                } else if (element instanceof IProject) {
                    return null;
                }
                return super.getText(element);
            }
        });

        view.getColumnWorktree().setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IEnvironment) {
                    return ((IEnvironment) element).getWorktreesBaseLocation().toString();
                } else if (element instanceof IProject) {
                    return null;
                }
                return super.getText(element);
            }
        });

        view.getColumnBranch().setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IEnvironment) {
                    return ((IEnvironment) element).getGemInternalBranchName();
                } else if (element instanceof IProject) {
                    return ((IProject) element).getBranchPrefix();
                }
                return super.getText(element);
            }
        });

        GridLayoutFactory.fillDefaults().generateLayout(parent);
        checkboxTreeViewer.setInput(this.obsoleteEnvironments);
        applyDialogFont(tableComposite);
        checkboxTreeViewer.expandAll();
        TreeColumn[] columns = checkboxTreeViewer.getTree().getColumns();
        for (TreeColumn column : columns) {
            column.pack();
        }
        checkboxTreeViewer.refresh();
        return tableComposite;
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
    public static List<IEnvironment> open(final Shell parentShell, final List<IProject> obsoleteEnvironments) {
        final ObsoleteEnvironmentDialog dialog = new ObsoleteEnvironmentDialog(parentShell, obsoleteEnvironments);
        if (dialog.open() == Window.CANCEL) {
            return Collections.emptyList();
        } else {
            if (dialog.selectedItems != null) {
                List<Object> checkedElementsList = Arrays.asList(dialog.selectedItems);
                return checkedElementsList.stream().filter(IEnvironment.class::isInstance)
                        .map(IEnvironment.class::cast).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}