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
package org.aposin.gem.ui.view.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.aposin.gem.ui.view.DynamicButtonGroupListView;
import org.aposin.gem.ui.view.DynamicButtonGroupListView.TYPE;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/**
 * Dashboard view containing a table and buttons next to it.
 */
public class DashboardView extends Composite {

    private final TableViewer tableViewer;
    private final Label dynamicButtonsLabel;
    private final DynamicButtonGroupListView dynamicButtons;

    private final Map<String, TableViewerColumn> columns = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param parent
     * @param style
     */
    public DashboardView(final Composite parent, final int style) {
        super(parent, style);
        GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(this);

        final Composite tableComposite = new Composite(this, SWT.NONE);
        tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
        tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.BORDER);
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout(tableColumnLayout);
        final Table repoTable = tableViewer.getTable();
        repoTable.setHeaderVisible(true);
        repoTable.setLinesVisible(true);

        dynamicButtonsLabel = new Label(this, SWT.NONE);
        dynamicButtonsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        dynamicButtons = new DynamicButtonGroupListView(this, TYPE.VERTICAL);
        dynamicButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        addControlListener(ControlListener.controlResizedAdapter(c -> {
            Object layoutData = getLayoutData();
            if (layoutData instanceof GridData) {
                ((GridData) layoutData).minimumHeight = computeMinimumHeight();
            }
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return super.computeSize(wHint, computeMinimumHeight(), changed);
    }

    /**
     * <p>Returns the minimum required height.</p>
     * 
     * <p>We don't want to recognize the table for the minimum height because a full table
     * means to calculate space for the whole content. Instead, only the label and the group
     * should be recognized.</p>
     * 
     * @return the minimum required height
     */
    private int computeMinimumHeight() {
        // @formatter:off
        return dynamicButtonsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
                + dynamicButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT).y 
                + 10; //TODO Try to find a better solution for calculation without magic numbers for borders, margins and indent.
        // @formatter:on
    }

    public final TableViewer getTableViewer() {
        return tableViewer;
    }

    public final Label getDynamicButtonsLabel() {
        return dynamicButtonsLabel;
    }

    public final DynamicButtonGroupListView getDynamicButtons() {
        return dynamicButtons;
    }

    /**
     * Gets the column by ID.
     * 
     * @param id
     * @return column
     */
    public TableViewerColumn getColumn(final String id) {
        return columns.get(id);
    }

    /**
     * Creates a column for the ID.
     * 
     * @param id
     */
    public final void createColumn(final String id) {
        // create the column with the layout
        final TableViewerColumn newColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        ((TableColumnLayout) tableViewer.getTable().getParent().getLayout())//
                .setColumnData(newColumn.getColumn(), new ColumnWeightData(1));

        // add to the columns list
        columns.put(id, newColumn);
    }

}
