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
package org.aposin.gem.ui.view;

import org.aposin.gem.ui.message.MessageRegistry;
import org.aposin.gem.ui.view.dashboard.DashboardView;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * View for the {@link org.aposin.gem.core.api.workflow.IEnvironmentWorkflow}.
 */
public final class EnvironmentWorkflowView extends Composite {

    private static final String NAME_COLUMN_ID = "NAME";
    private static final String BASE_BRANCH_COLUMN_ID = "BASE_BRANCH";
    private static final String STATUS_COLUMN_ID = "STATUS";
    private static final String WT_LOCATION_COLUMN_ID = "WT_LOCATION";
    
    // selection components for the environment
    private final Label projectLabel;
    private final ComboViewer projectSelectionComboViewer;
    private final Label environmentLabel;
    private final ComboViewer environmentSelectionComboViewer;

    // buttons for each step of the workflow
    private final Button cloneRepositoriesButton;
    private final Button synchronizeAllEnvBranchesButton;
    private final Button setupWorktreeButton;
    private final Button removeWorktreeButton;

    // repository information as a table
    private final Label repositoryInfoLabel;
    private final DashboardView dashboardView;

    public EnvironmentWorkflowView(final Composite parent, final int style) {
        super(parent, style);
        setLayout(new FillLayout());

        ScrolledComposite sc = new ScrolledComposite(this, SWT.V_SCROLL);
        Composite composite = new Composite(sc, SWT.NONE);
        sc.setContent(composite);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        addControlListener(ControlListener
                .controlResizedAdapter(c -> sc.setMinHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)));

        composite.setLayout(new GridLayout(3, true));

        projectLabel = new Label(composite, SWT.NONE);
        environmentLabel = new Label(composite, SWT.NONE);
        environmentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        final GridDataFactory factory =
                GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

        projectSelectionComboViewer = ViewHelper.createProjectCombo(composite);
        factory.applyTo(projectSelectionComboViewer.getControl());

        environmentSelectionComboViewer = ViewHelper.createEnvironmentCombo(composite);
        factory.applyTo(environmentSelectionComboViewer.getControl());

        final Composite firstButtonsComposite = new Composite(composite, SWT.NONE);
        factory.applyTo(firstButtonsComposite);
        firstButtonsComposite.setLayout(new FillLayout());

        cloneRepositoriesButton = new Button(firstButtonsComposite, SWT.NONE);
        setupWorktreeButton = new Button(firstButtonsComposite, SWT.NONE);

        final Label emptyLabel = new Label(composite, SWT.NONE);
        emptyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        final Composite secondButtonsComposite = new Composite(composite, SWT.NONE);
        factory.applyTo(secondButtonsComposite);
        secondButtonsComposite.setLayout(new FillLayout());

        synchronizeAllEnvBranchesButton = new Button(secondButtonsComposite, SWT.NONE);
        removeWorktreeButton = new Button(secondButtonsComposite, SWT.NONE);

        // table with repository information
        repositoryInfoLabel = new Label(composite, SWT.NONE);
        repositoryInfoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        dashboardView = new DashboardView(composite, SWT.NONE);
        dashboardView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        dashboardView.createColumn(NAME_COLUMN_ID);
        dashboardView.createColumn(BASE_BRANCH_COLUMN_ID);
        dashboardView.createColumn(STATUS_COLUMN_ID);
        dashboardView.createColumn(WT_LOCATION_COLUMN_ID);
    }

    /**
     * Register the components that should be translated.
     * 
     * @param registry the registry.
     */
    public void registerMessages(final MessageRegistry registry) {
        registry.register(projectLabel::setText, messages -> messages.project_label_common);
        registry.register(environmentLabel::setText, messages -> messages.environment_label_common);
        registry.register(cloneRepositoriesButton::setText,
                messages -> messages.workflowCloneRepositories_label_common);
        registry.register(setupWorktreeButton::setText,
                messages -> messages.workflowSetupWorktree_label_common);
        registry.register(synchronizeAllEnvBranchesButton::setText,
                messages -> messages.workflowSynchronizeAllEnvBranches_label_common);
        registry.register(removeWorktreeButton::setText,
                messages -> messages.workflowRemoveWorktree_label_common);

        registry.register(repositoryInfoLabel::setText, messages -> messages.repository_label_common);

        registry.register(s -> getRepoNameViewerColumn().getColumn().setText(s),
                messages -> messages.environmentWorkflowView_label_dashboardViewNameColumn);
        
        registry.register(s -> getBaseBranchViewerColumn().getColumn().setText(s),
                messages -> messages.dashboardView_label_baseBranchColumn);
        registry.register(s -> getBaseBranchViewerColumn().getColumn().setToolTipText(s),
                messages -> messages.dashboardView_tooltip_baseBranchColumn);
        
        registry.register(s -> getRepoStatusViewerColumn().getColumn().setText(s),
                messages -> messages.dashboardView_label_baseBranchColumn);
        
        registry.register(s -> getRepoWorktreeLocationViewerColumn().getColumn().setText(s),
                messages -> messages.environmentWorkflowView_label_dashboardViewWorktreeLocationColumn);
    }

    public ComboViewer getProjectSelectionComboViewer() {
        return projectSelectionComboViewer;
    }

    public ComboViewer getEnvironmentSelectionComboViewer() {
        return environmentSelectionComboViewer;
    }

    public Button getCloneRepositoriesButton() {
        return cloneRepositoriesButton;
    }

    public Button getSetupWorktreeButton() {
        return setupWorktreeButton;
    }

    public Button getSynchronizeAllEnvBranchesButton() {
        return synchronizeAllEnvBranchesButton;
    }

    public Button getRemoveWorktreeButton() {
        return removeWorktreeButton;
    }

    public DashboardView getDashboardView() {
        return dashboardView;
    }

    public TableViewerColumn getRepoNameViewerColumn() {
        return dashboardView.getColumn(NAME_COLUMN_ID);
    }

    public TableViewerColumn getBaseBranchViewerColumn() {
        return dashboardView.getColumn(BASE_BRANCH_COLUMN_ID);
    }

    public TableViewerColumn getRepoStatusViewerColumn() {
        return dashboardView.getColumn(STATUS_COLUMN_ID);
    }

    public TableViewerColumn getRepoWorktreeLocationViewerColumn() {
        return dashboardView.getColumn(WT_LOCATION_COLUMN_ID);
    }

    public DynamicButtonGroupListView getByRepositoryLauncherButtons() {
        return dashboardView.getDynamicButtons();
    }
    
}
