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
package org.aposin.gem.ui.message;

import org.eclipse.e4.core.services.nls.Message;

/**
 * Messages for internationalization.
 */
@Message
public class Messages {

    ///////////////////////////////
    // dialog labels
    public String quitHandler_titleFormat_dialog;
    public String quitHandler_message_dialog;
    public String aboutHandler_titleFormat_dialog;
    public String aboutHandler_messageFormat_dialog;
    public String mergeConflictDialog_titleFormat_dialog;
    public String mergeConflictDialog_messageFormat_dialog;
    public String mergeConflictDialog_label_continueButton;
    public String mergeConflictDialog_label_abortButton;
    public String commandsRunnable_messageFormat_progressMonitorStart;
    public String commandsRunnable_messageFormat_progressMonitorUpdate;
    public String commandsRunnable_message_cancelled;
    public String commandsRunnable_message_noFailure;
    public String cliProgressMonitorDialog_label_detailsButton;
    public String commandProgressDialog_message_successDialog;
    public String commandProgressDialog_messageFormat_statusWithErrors;

    ///////////////////////////////
    // handler labels
    public String refreshSessionHandler_message_progressMonitorStart;

    ///////////////////////////////
    // lifecycle labels
    public String sessionInitializer_message_progressMonitorStart;
    public String sessionInitializer_message_progressMonitorLoadSession;
    public String sessionInitializer_message_progressMonitorLoadPrefs;
    public String sessionInitializer_message_progressMonitorLoadConfig;
    public String sessionInitializer_title_openQuestionDialog;
    public String sessionInitializer_messageFormat_checkoutDifferentBranchQuestionDialog;
    public String sessionInitializer_messageFormat_pullFailsProceedQuestionDialog;

    ///////////////////////////////
    // model common labels
    public String repository_label_common;
    public String project_label_common;
    public String environment_label_common;
    public String featureBranch_label_common;
    public String featureBranchProvider_label_common;
    public String worktree_label_common;

    ///////////////////////////////
    // workflow common labels
    public String workflowRequiresClone_label_common;
    public String workflowCloneRepositories_label_common;
    public String workflowSynchronizeAllEnvBranches_label_common;
    public String workflowRequiresWorktreeSetup_label_common;
    public String workflowSetupWorktree_label_common;
    public String workflowRemoveWorktree_label_common;
    public String workflowRemoveWorktree_warningMessageFormat_common;
    public String workflowFetchAndCheckoutFeatureBranch_label_common;
    public String workflowMergeBaseIntoFeatureBranch_label_common;
    public String workflowPullFeatureBranch_label_common;
    public String workflowCleanWorktree_label_common;
    public String workflowCleanWorktree_warningMessageFormat_common;
    public String workflowRemoveFeatureBranch_label_common;
    public String workflowRemoveFeatureBranch_warningMessageFormat_common;

    ///////////////////////////////
    // LocalRepoStatus labels (unrelated with workflow)
    public String localRepoStatusPlaceholder_label_uiString;
    public String localRepoStatusNotAvailable_label_uiString;
    public String localRepoStatusReady_label_uiString;
    public String localRepoStatusRequiresClone_label_uiString;
    public String localRepoStatusRequiresWorktree_label_uiString;
    public String localRepoStatusRequiresWorktreeRemoval_label_uiString;
    public String localRepoStatusRequiresFbCheckout_label_uiString;
    
    ///////////////////////////////
    // Dashboard labels
    public String dashboardView_labelFormat_selectionDescription;
    public String dashboardView_label_statusColumn;
    public String dashboardView_label_baseBranchColumn;
    public String dashboardView_tooltip_baseBranchColumn;
    
    // EnvironmentWorkflowView dashboard labels
    public String environmentWorkflowView_label_dashboardViewNameColumn;
    public String environmentWorkflowView_label_dashboardViewWorktreeLocationColumn;
    
    // FeatureBranchWorkflowView dashboard labels
    public String featureBranchWorkflowView_label_dashboardViewTargetBranchColumn;
    public String featureBranchWorkflowView_tooltip_dashboardViewTargetBranchColumn;
    public String featureBranchWorkflowView_label_dashboardViewWorktreeDefinitionColumn;
    public String featureBranchWorkflowView_tooltip_dashboardViewWorktreeDefinitionColumn;

    // cleanObsoleteEnvironments 
    public String cleanObsoleteEnvironmentsHandler_message_fetchWorktreesProgressMonitor;
    public String cleanObsoleteEnvironmentsHandler_message_noWorktreesAvailableDialog;
    public String cleanObsoleteEnvironmentsHandler_message_confirmDeleteDialog;
    public String cleanObsoleteEnvironmentsHandler_message_nothingSelectedDialog;
}
