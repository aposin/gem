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
import org.aposin.gem.ui.view.DynamicButtonGroupListView.TYPE;
import org.aposin.gem.ui.view.dashboard.DashboardView;
import org.aposin.gem.ui.view.fieldassist.ComboViewerAutoCompleteField;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * View for the {@link org.aposin.gem.core.api.workflow.IFeatureBranchWorkflow}.
 */
public final class FeatureBranchWorkflowView extends Composite {

    private static final String WT_DEFINITION_COLUMN_ID = "WT_DEFINITION";
    private static final String STATUS_COLUMN_ID = "STATUS";
    private static final String BASE_BRANCH_COLUMN_ID = "BASE_BRANCH";
    private static final String TARGET_BRANCH_COLUMN_ID = "TARGET_BRANCH";
    
    // selection components for the environment
    private final Label projectLabel;
    private final ComboViewer projectSelectionComboViewer;
    private final Label environmentLabel;
    private final ComboViewer environmentSelectionComboViewer;

    // selection components for the feature branches
    private final Label featureBranchProviderLabel;
    private final ComboViewer selectFeatureBranchProviderComboViewer;
    private final ControlDecoration featureBranchSelectorInactiveDecoration;
    private final Label featureBranchLabel;
    private final ComboViewerAutoCompleteField selectFeatureBranchAutoCompleteField;

    // env-workflow button, but makes sense here too
    private final Button synchronizeAllEnvBranchesButton;
    // buttons for each step of the workflow
    private final Button checkoutFeatureBranchButton;
    private final Button mergeBaseIntoFeatureBranchButton;
    private final Button pullFeatureBranchButton;
    private final Button cleanWorktreeButton;
    private final Button removeFeatureBranchButton;

    // worktree information as a table
    private final Label repositoryInfoLabel;
    private final DashboardView dashboardView;

    private final DynamicButtonGroupListView featureBranchLauncherButtons;


    public FeatureBranchWorkflowView(final Composite parent, final int style) {
        super(parent, style);
        setLayout(new GridLayout(3, true));

        final GridDataFactory factory =
                GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

        projectLabel = new Label(this, SWT.NONE);
        environmentLabel = new Label(this, SWT.NONE);
        environmentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        projectSelectionComboViewer = ViewHelper.createProjectCombo(this);
        final Combo projectSelectionCombo = projectSelectionComboViewer.getCombo();
        factory.applyTo(projectSelectionCombo);
        environmentSelectionComboViewer = ViewHelper.createEnvironmentCombo(this);
        final Combo environmentSelectionCombo = environmentSelectionComboViewer.getCombo();
        factory.applyTo(environmentSelectionCombo);
        final Composite environmentButtonComposite = new Composite(this, SWT.NONE);
        factory.applyTo(environmentButtonComposite);
        environmentButtonComposite.setLayout(new FillLayout());

        // environment buttons
        synchronizeAllEnvBranchesButton = new Button(environmentButtonComposite, SWT.NONE);
        mergeBaseIntoFeatureBranchButton = new Button(environmentButtonComposite, SWT.NONE);

        featureBranchProviderLabel = new Label(this, SWT.NONE);
        featureBranchLabel = new Label(this, SWT.NONE);
        featureBranchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        featureBranchSelectorInactiveDecoration =
                new ControlDecoration(featureBranchLabel, SWT.TOP | SWT.RIGHT);
        featureBranchSelectorInactiveDecoration.setShowOnlyOnFocus(false);


        selectFeatureBranchProviderComboViewer = new ComboViewer(this, SWT.READ_ONLY);
        final Combo selectFeatureBranchProviderCombo =
                selectFeatureBranchProviderComboViewer.getCombo();
        factory.applyTo(selectFeatureBranchProviderCombo);
        
        // not using SWT.READ_ONLY to allow auto-complete field editable to search
        final ComboViewer selectFeatureBranchComboViewer = new ComboViewer(this, SWT.NONE);
        factory.applyTo(selectFeatureBranchComboViewer.getCombo());
        selectFeatureBranchAutoCompleteField = new ComboViewerAutoCompleteField(selectFeatureBranchComboViewer);
        
        final Composite firstFbButtonsComposite = new Composite(this, SWT.NONE);
        factory.applyTo(firstFbButtonsComposite);
        firstFbButtonsComposite.setLayout(new FillLayout());

        checkoutFeatureBranchButton = new Button(firstFbButtonsComposite, SWT.NONE);
        pullFeatureBranchButton = new Button(firstFbButtonsComposite, SWT.NONE);

        final Label emptyLabel = new Label(this, SWT.NONE);
        emptyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        final Composite secondFbButtonsComposite = new Composite(this, SWT.NONE);
        factory.applyTo(secondFbButtonsComposite);
        secondFbButtonsComposite.setLayout(new FillLayout());

        cleanWorktreeButton = new Button(secondFbButtonsComposite, SWT.NONE);
        removeFeatureBranchButton = new Button(secondFbButtonsComposite, SWT.NONE);

        repositoryInfoLabel = new Label(this, SWT.NONE);
        repositoryInfoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));

        dashboardView = new DashboardView(this, SWT.NONE);
        dashboardView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
        
        dashboardView.createColumn(WT_DEFINITION_COLUMN_ID);
        dashboardView.createColumn(STATUS_COLUMN_ID);
        dashboardView.createColumn(BASE_BRANCH_COLUMN_ID);
        dashboardView.createColumn(TARGET_BRANCH_COLUMN_ID);
        
        featureBranchLauncherButtons = new DynamicButtonGroupListView(this, TYPE.HORIZONTAL);
        featureBranchLauncherButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
    }

    /**
     * Register the components that should be translated.
     * 
     * @param registry the registry.
     */
    public void registerMessages(final MessageRegistry registry) {
        registry.register(projectLabel::setText, messages -> messages.project_label_common);
        registry.register(environmentLabel::setText, messages -> messages.environment_label_common);

        registry.register(featureBranchProviderLabel::setText,
                messages -> messages.featureBranchProvider_label_common);
        registry.register(featureBranchLabel::setText, messages -> messages.featureBranch_label_common);

        registry.register(synchronizeAllEnvBranchesButton::setText,
                messages -> messages.workflowSynchronizeAllEnvBranches_label_common);

        registry.register(checkoutFeatureBranchButton::setText,
                messages -> messages.workflowFetchAndCheckoutFeatureBranch_label_common);
        registry.register(mergeBaseIntoFeatureBranchButton::setText,
                messages -> messages.workflowMergeBaseIntoFeatureBranch_label_common);
        registry.register(pullFeatureBranchButton::setText,
                messages -> messages.workflowPullFeatureBranch_label_common);
        registry.register(removeFeatureBranchButton::setText,
                messages -> messages.workflowRemoveFeatureBranch_label_common);
        registry.register(cleanWorktreeButton::setText, //
                messages -> messages.workflowCleanWorktree_label_common);

        registry.register(repositoryInfoLabel::setText, messages -> messages.repository_label_common);

        registry.register(s -> getWorktreeDefinitionColumn().getColumn().setText(s),
                messages -> messages.featureBranchWorkflowView_label_dashboardViewWorktreeDefinitionColumn);
        registry.register(s -> getWorktreeDefinitionColumn().getColumn().setToolTipText(s),
                messages -> messages.featureBranchWorkflowView_tooltip_dashboardViewWorktreeDefinitionColumn);
        
        registry.register(s -> getStatusColumn().getColumn().setText(s),
                messages -> messages.dashboardView_label_statusColumn);
        
        registry.register(s -> getBaseBranchColumn().getColumn().setText(s),
                messages -> messages.dashboardView_label_baseBranchColumn);
        registry.register(s -> getBaseBranchColumn().getColumn().setToolTipText(s),
                messages -> messages.dashboardView_tooltip_baseBranchColumn);
        
        registry.register(s -> getTargetBranchColumn().getColumn().setText(s),
                messages -> messages.featureBranchWorkflowView_label_dashboardViewTargetBranchColumn);
        registry.register(s -> getBaseBranchColumn().getColumn().setToolTipText(s),
                messages -> messages.featureBranchWorkflowView_tooltip_dashboardViewTargetBranchColumn);
    }

    public ComboViewer getProjectSelectionComboViewer() {
        return projectSelectionComboViewer;
    }

    public ComboViewer getEnvironmentSelectionComboViewer() {
        return environmentSelectionComboViewer;
    }

    public ComboViewer getFeatureBranchProviderComboViewer() {
        return selectFeatureBranchProviderComboViewer;
    }

    public ControlDecoration getFeatureBranchSelectorInactiveDecoration() {
        return featureBranchSelectorInactiveDecoration;
    }

    public ComboViewerAutoCompleteField getFeatureBranchAutoCompleteField() {
        return selectFeatureBranchAutoCompleteField;
    }
    
    public Button getCheckoutFeatureBranchButton() {
        return checkoutFeatureBranchButton;
    }

    public Button getSynchronizeAllEnvBranchesButton() {
        return synchronizeAllEnvBranchesButton;
    }

    public Button getMergeBaseIntoFeatureBranchButton() {
        return mergeBaseIntoFeatureBranchButton;
    }

    public Button getPullFeatureBranchButton() {
        return pullFeatureBranchButton;
    }

    public Button getCleanWorktreeButton() {
        return cleanWorktreeButton;
    }
    
    public Button getRemoveFeatureBranchButton() {
        return removeFeatureBranchButton;
    }
    
    public DashboardView getDashboardView() {
        return dashboardView;
    }

    public TableViewerColumn getWorktreeDefinitionColumn() {
        return dashboardView.getColumn(WT_DEFINITION_COLUMN_ID);
    }

    public TableViewerColumn getStatusColumn() {
        return dashboardView.getColumn(STATUS_COLUMN_ID);
    }

    public TableViewerColumn getBaseBranchColumn() {
        return dashboardView.getColumn(BASE_BRANCH_COLUMN_ID);
    }

    public TableViewerColumn getTargetBranchColumn() {
        return dashboardView.getColumn(TARGET_BRANCH_COLUMN_ID);
    }

    public DynamicButtonGroupListView getFeatureBranchLauncherButtons() {
        return featureBranchLauncherButtons;
    }

}
