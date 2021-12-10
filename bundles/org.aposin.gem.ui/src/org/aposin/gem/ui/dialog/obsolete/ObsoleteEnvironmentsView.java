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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * View for obsolete environments
 */
class ObsoleteEnvironmentsView extends Composite {

    private final CheckboxTreeViewer treeViewer;
    private final TreeViewerColumn columnProject;
    private final TreeViewerColumn columnEnvironment;
    private final TreeViewerColumn columnWorktree;
    
    private final Button selectAllButton;

    /**
     * Constructor for Obsolete Environments View
     * @param parent
     * @param style
     */
    public ObsoleteEnvironmentsView(final Composite parent, int style) {
        super(parent, style);
        GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(this);
        final Composite tableComposite = new Composite(this,
                SWT.FILL);
        tableComposite.setLayoutData(
                new GridData(SWT.FILL, style, true, true));
        treeViewer = new CheckboxTreeViewer(tableComposite,
                SWT.FILL | SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER);

        final TreeColumnLayout treeColumnLayout = new TreeColumnLayout(true);
        tableComposite.setLayout(treeColumnLayout);

        final Tree tree = getCheckboxTreeViewer().getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        columnProject = createNewColumn(1);
        columnEnvironment = createNewColumn(1);
        columnWorktree = createNewColumn(2);

        selectAllButton = new Button(this, SWT.CHECK);
        selectAllButton.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, false, false));
    }

    private TreeViewerColumn createNewColumn(final int weightData) {
        final TreeViewerColumn column = new TreeViewerColumn(getCheckboxTreeViewer(), GridData.FILL);
        ((TreeColumnLayout) getCheckboxTreeViewer().getTree().getParent().getLayout()).setColumnData(column.getColumn(),
                new ColumnWeightData(weightData));
        return column;
    }

    /**
     * @return tree viewer for obsolete worktree
     */
    public CheckboxTreeViewer getCheckboxTreeViewer() {
        return treeViewer;
    }

    /**
     * @return project column
     */
    public TreeViewerColumn getColumnProject() {
        return columnProject;
    }

    /**
     * @return environment column
     */
    public TreeViewerColumn getColumnEnvironment() {
        return columnEnvironment;
    }

    /**
     * @return worktree column
     */
    public TreeViewerColumn getColumnWorktree() {
        return columnWorktree;
    }

    /**
     * @return select all button
     */
    public Button getSelectAllButton() {
        return selectAllButton;
    }

}
