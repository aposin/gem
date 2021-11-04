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
package org.aposin.gem.ui.part;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.service.IFeatureBranchProvider;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.ui.dialog.MergeConflictDialog;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.lifecycle.event.EnvironmentSynchronizedEvent;
import org.aposin.gem.ui.lifecycle.event.RefreshedObjectEvent;
import org.aposin.gem.ui.lifecycle.event.SessionEnvironmentChangeEvent;
import org.aposin.gem.ui.lifecycle.event.SessionFeatureBranchChangeEvent;
import org.aposin.gem.ui.message.MessageRegistry;
import org.aposin.gem.ui.part.listener.EnvironmentSelectionListener;
import org.aposin.gem.ui.part.listener.FeatureBranchSelectionListener;
import org.aposin.gem.ui.part.listener.ProjectSelectionListener;
import org.aposin.gem.ui.part.listener.dashboard.DashboardDynamicButtonListener;
import org.aposin.gem.ui.part.listener.dashboard.LauncherDynamicMenuListener;
import org.aposin.gem.ui.part.listener.workflow.EnvironmentWorkflowOnSelectionListener;
import org.aposin.gem.ui.part.listener.workflow.FeatureBranchWorkflowOnSelectionListener;
import org.aposin.gem.ui.process.CallbackRunnable;
import org.aposin.gem.ui.process.LocalRepoStatus;
import org.aposin.gem.ui.process.launcher.NullLauncher;
import org.aposin.gem.ui.process.launcher.SwitchLauncher;
import org.aposin.gem.ui.view.FeatureBranchWorkflowView;
import org.aposin.gem.ui.view.dashboard.FeatureBranchesDashboardInfo;
import org.aposin.gem.ui.view.dashboard.RepositoryDashboardInfoContainer;
import org.aposin.gem.ui.view.fieldassist.ComboViewerAutoCompleteField;
import org.aposin.gem.ui.view.fieldassist.ComboViewerAutoCompleteFieldHandler;
import org.aposin.gem.ui.view.labelprovider.NamedObjectLabelProvider;
import org.aposin.gem.ui.view.labelprovider.TypedColumnLabelProvider.TypedColumnLabelProviderFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureBranchesPart {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureBranchesPart.class);

    private FeatureBranchWorkflowView view;
    private DashboardDynamicButtonListener dashboardListener;

    @Inject
    private IThemeEngine themeEngine;

    @Inject
    private MessageRegistry registry;

    @Inject
    private Session session;

    @Inject
    private UISynchronize uiSynchronize;

    @PostConstruct
    public void postConstruct(Composite parent) {
        view = new FeatureBranchWorkflowView(parent, SWT.NONE);
        view.registerMessages(registry);
        createComponents();
        // init selection and refresh view before (avoid triggering listeners)
        initSelection();
        refreshView();
        registerListeners();
    }

    private void createComponents() {
        createComboViewers();
        setupTableViewer();
        recreateLauncherButtons(session.getSessionFeatureBranch());
    }

    private void initSelection() {
        PartHelper.selectEnvironmentOnCombos(session.getSessionEnvironment(),
                view.getProjectSelectionComboViewer(), view.getEnvironmentSelectionComboViewer());
        view.getFeatureBranchProviderComboViewer()
                .setSelection(new StructuredSelection(session.getSessionFeatureBranchProvider()));
        if (session.getSessionFeatureBranch() != null) {
            view.getFeatureBranchAutoCompleteField()
                    .setSelection(session.getSessionFeatureBranch());
        }
    }

    private void refreshView() {
        refillFeatureBranchesCombo(session.getSessionEnvironment(),
                session.getSessionFeatureBranchProvider(), false);
        resetDashboardComponents(session.getSessionEnvironment(),
                session.getSessionFeatureBranch());
    }

    private void createComboViewers() {
        PartHelper.setEnvironmentInformationData(session, view.getProjectSelectionComboViewer(),
                view.getEnvironmentSelectionComboViewer());

        view.getFeatureBranchProviderComboViewer()
                .setContentProvider(ArrayContentProvider.getInstance());
        view.getFeatureBranchProviderComboViewer()
                .setLabelProvider(new NamedObjectLabelProvider(IFeatureBranchProvider.class));
        // add the input with the providers information
        view.getFeatureBranchProviderComboViewer().setInput(
                session.getConfiguration().getServiceContainer().getFeatureBranchProviders());

        view.getFeatureBranchAutoCompleteField()
                .setLabelProvider(new NamedObjectLabelProvider(IFeatureBranch.class));
        view.getFeatureBranchAutoCompleteField().setHandler(new ComboViewerAutoCompleteFieldHandler() {
            
            @Override
            public void handleNoProposalAccepted(ComboViewerAutoCompleteField field) {
                logger.debug("Reset viewer to session FB: {}", session.getSessionFeatureBranch());
                field.setSelection(session.getSessionFeatureBranch());
            }
        });
    }

    private void setupTableViewer() {
        // create the columns
        TypedColumnLabelProviderFactory.create(FeatureBranchesDashboardInfo.class) //
                .text(FeatureBranchesDashboardInfo::getDashboardDescriptionName) //
                .image(info -> info.getStatus().getStatusDecoratorImage()) //
                .set(view.getWorktreeDefinitionColumn());
        TypedColumnLabelProviderFactory.create(FeatureBranchesDashboardInfo.class) //
                .text(e -> e.getStatus().getStatusString(registry.getMessages())) //
                .set(view.getStatusColumn());
        TypedColumnLabelProviderFactory.create(FeatureBranchesDashboardInfo.class) //
                .text(FeatureBranchesDashboardInfo::getBaseBranch) //
                .set(view.getBaseBranchColumn());
        TypedColumnLabelProviderFactory.create(FeatureBranchesDashboardInfo.class) //
                .text(FeatureBranchesDashboardInfo::getTargetBranchName) //
                .set(view.getTargetBranchColumn());
    }

    private void recreateLauncherButtons(final IFeatureBranch featureBranch) {
        final List<ILauncher> featureBranchLaunchers = new ArrayList<>();
        if (featureBranch == null) {
            // TODO: externalize strings!
            featureBranchLaunchers.add(new NullLauncher("No FB selected"));
        } else {
            // by feature branch
            featureBranchLaunchers.addAll(featureBranch.getLaunchers());
        }

        if (featureBranchLaunchers.isEmpty()) {
            featureBranchLaunchers.add(new NullLauncher("None"));
        }

        PartHelper.recreateLauncherButtons(view.getFeatureBranchLauncherButtons(),
                featureBranchLaunchers, PartHelper.getActiveTheme(themeEngine));
    }

    @Optional
    @Inject
    private void onThemeChanged(@UIEventTopic(IThemeEngine.Events.THEME_CHANGED) Object obj) {
        recreateLauncherButtons(session.getSessionFeatureBranch());
        dashboardListener.trigger();
    }

    private void registerListeners() {
        // listeners
        view.getProjectSelectionComboViewer().addSelectionChangedListener(
                new ProjectSelectionListener(session, view.getEnvironmentSelectionComboViewer()));
        view.getEnvironmentSelectionComboViewer()
                .addSelectionChangedListener(new EnvironmentSelectionListener(session));
        view.getFeatureBranchAutoCompleteField()
                .addSelectionChangedListener(new FeatureBranchSelectionListener(session));

        view.getFeatureBranchProviderComboViewer()
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    @Override
                    public void selectionChanged(final SelectionChangedEvent event) {
                        final IStructuredSelection selection = event.getStructuredSelection();
                        if (!selection.isEmpty()) {
                            final IFeatureBranchProvider selectedProvider =
                                    (IFeatureBranchProvider) selection.getFirstElement();
                            // update the session feature branch if it is null or it is from a
                            // different provider
                            if (session.getSessionFeatureBranch() == null || //
                                    !Objects.equals(selectedProvider, session.getSessionFeatureBranchProvider())) {
                                refillFeatureBranchesCombo(session.getSessionEnvironment(),
                                        selectedProvider, false);
                            }
                        }
                    }
                });

        // action button listeners
        PartHelper.addButtonLauncherListener(view.getCheckoutFeatureBranchButton(),
                new FeatureBranchWorkflowOnSelectionListener(session,
                        fb -> fb.getWorkflow().getFetchAndCheckoutLauncher()));
        PartHelper.addButtonLauncherListener(view.getSynchronizeAllEnvBranchesButton(),
                new EnvironmentWorkflowOnSelectionListener(session, env -> env.getWorkflow()//
                        .getSynchronizeAllEnvBranchesLauncher()));
        PartHelper.addButtonLauncherListener(view.getMergeBaseIntoFeatureBranchButton(),
                new FeatureBranchWorkflowOnSelectionListener(session, fb -> fb.getWorkflow()
                        .getMergeBaseIntoFeatureBranchLauncher(wt -> askMergeConflictsAbort(fb, wt))));
        PartHelper.addButtonLauncherListener(view.getPullFeatureBranchButton(),
                new FeatureBranchWorkflowOnSelectionListener(session,
                        fb -> fb.getWorkflow().getPullLauncher(wt -> askMergeConflictsAbort(fb, wt))));
        PartHelper.addButtonLauncherListener(view.getCleanWorktreeButton(), //
                new FeatureBranchWorkflowOnSelectionListener(session, //
                        fb -> new SwitchLauncher(fb.getWorkflow().getCleanWorktreeLauncher(), () -> //
                        MessageDialog.openConfirm(null, //
                                registry.getMessages().workflowCleanWorktree_label_common,
                                MessageFormat.format(// 
                                        registry.getMessages().workflowCleanWorktree_warningMessageFormat_common,
                                        fb.getEnvironment().getProject().getDisplayName(),
                                        fb.getEnvironment().getDisplayName())))));

        PartHelper.addButtonLauncherListener(view.getRemoveFeatureBranchButton(), //
                new FeatureBranchWorkflowOnSelectionListener(session, //
                        fb -> new SwitchLauncher(fb.getWorkflow().getRemoveBranchLauncher(), () -> //
                        MessageDialog.openConfirm(null, //
                                registry.getMessages().workflowRemoveFeatureBranch_label_common, //
                                MessageFormat.format(//
                                        registry.getMessages().workflowRemoveFeatureBranch_warningMessageFormat_common, //
                                        fb.getDisplayName())))));

        // also register the context menu and the dashboard buttons
        LauncherDynamicMenuListener
                .registerListener(view.getDashboardView().getTableViewer().getTable());
        dashboardListener = DashboardDynamicButtonListener.registerListener(//
                view.getDashboardView(), registry, themeEngine);
    }

    private boolean askMergeConflictsAbort(final IFeatureBranch fb, final IWorktreeDefinition worktree) {
        final AtomicBoolean question = new AtomicBoolean(false);
        try {
            uiSynchronize.syncExec(() -> // negate as cancel is abort
                question.set(!MergeConflictDialog.openContinue(registry.getMessages(), PartHelper.getActiveTheme(themeEngine), worktree)));
        } catch (final Exception e) {
            LOGGER.debug("Ignored exception", e);
        }
        return question.get();
    }

    // TODO - should separate betweern environment and feature branch information
    // TODO - to be able to trigger update of different information without refetching
    // TODO - already available data
    private void resetDashboardComponents(final IEnvironment environment,
            final IFeatureBranch featureBranch) {
        final List<IWorktreeDefinition> envWorktrees = environment.getEnvironmentWorktrees();
        final List<RepositoryDashboardInfoContainer> dashboardInfo =
                new ArrayList<>(envWorktrees.size());
        for (final IWorktreeDefinition worktreeDef : envWorktrees) {

            final FeatureBranchesDashboardInfo info =
                    toRepoDashboardInfoWithCallback(worktreeDef, environment, featureBranch);

            dashboardInfo.add(info);
        }
        PartHelper.setDashboardContent(view.getDashboardView(), dashboardInfo);
    }

    private final FeatureBranchesDashboardInfo toRepoDashboardInfoWithCallback(
            final IWorktreeDefinition worktree, final IEnvironment environment,
            final IFeatureBranch featureBranch) {
        final FeatureBranchesDashboardInfo info = new FeatureBranchesDashboardInfo(//
                featureBranch, environment, //
                environment.getEnvironmentWorktrees().indexOf(worktree));
        final CallbackRunnable<LocalRepoStatus> callback = new CallbackRunnable<LocalRepoStatus>() {

            @Override
            public LocalRepoStatus doRun() {
                return info.computeStatus();
            }

            @Override
            public void callback(final LocalRepoStatus value) {
                // only if something doesn't fail
                if (value != null) {
                    view.getDashboardView().getTableViewer().refresh();
                }
            }
        };

        uiSynchronize.asyncExec(callback);

        return info;
    }

    private void refillFeatureBranchesCombo(final IEnvironment environment,
            final IFeatureBranchProvider provider, final boolean refreshProvider) {
        // first remove all combo input/proposals
        view.getFeatureBranchAutoCompleteField().clearInput();
        // if it requires cloning, disable the combo
        if (environment.getWorkflow().getCloneLauncher().canLaunch()) {
            disableFeatureBranchComboWithWarning(
                    registry.getMessages().workflowRequiresClone_label_common);
        } else {
            disableFeatureBranchComboWithWarning("Fetching feature branches");
            if (refreshProvider) {
                // TODO - add as a callback - expected time to refresh will be a bit long...?
                provider.refresh();
            }
            final List<IFeatureBranch> featureBranches = provider.getFeatureBranches(environment);
            view.getFeatureBranchAutoCompleteField().setInput(featureBranches);
            
            if (featureBranches.isEmpty()) {
                disableFeatureBranchComboWithWarning("No feature-branches for the provider");
                session.setSessionFeatureBranch(null);
            } else {
                view.getFeatureBranchSelectorInactiveDecoration().setImage(null);
                view.getFeatureBranchSelectorInactiveDecoration().setDescriptionText("");
                view.getFeatureBranchAutoCompleteField().setEnabled(true);
                final IFeatureBranch selected = provider // get the matching feature
                        .getMatchingFeatureBranch(environment, session.getSessionFeatureBranch()) //
                        // or the default, which should always return non-null if there are FBs
                        .or(() -> provider.getDefaultFeatureBranch(environment)) //
                        .orElse(null);
                // this should never happen
                if (selected == null) {
                    throw new GemException("Internal error: selected FB must not be null");
                }
                // finally, set the selection
                view.getFeatureBranchAutoCompleteField().setSelection(selected);
            }
        }
        setButtonEnablement();
        view.getFeatureBranchAutoCompleteField().refresh();
    }

    private void setButtonEnablement() {
        PartHelper.setLauncherButtonEnabled(view.getCheckoutFeatureBranchButton());
        PartHelper.setLauncherButtonEnabled(view.getSynchronizeAllEnvBranchesButton());
        PartHelper.setLauncherButtonEnabled(view.getMergeBaseIntoFeatureBranchButton());
        PartHelper.setLauncherButtonEnabled(view.getMergeBaseIntoFeatureBranchButton());
        PartHelper.setLauncherButtonEnabled(view.getPullFeatureBranchButton());
        PartHelper.setLauncherButtonEnabled(view.getCleanWorktreeButton());
        PartHelper.setLauncherButtonEnabled(view.getRemoveFeatureBranchButton());
    }

    private void disableFeatureBranchComboWithWarning(final String warningMessage) {
        final Image warningIcon = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
        view.getFeatureBranchSelectorInactiveDecoration().setImage(warningIcon);
        view.getFeatureBranchSelectorInactiveDecoration().setDescriptionText(warningMessage);
        view.getFeatureBranchAutoCompleteField().setEnabled(false);
    }

    /**
     * 
     * @param event
     */
    @Optional
    @Inject
    public void onEnvironmentSynchronized(
            @UIEventTopic(EnvironmentSynchronizedEvent.TOPIC) EnvironmentSynchronizedEvent event) {
        if (Objects.equals(event.synchronizedEnvironment, session.getSessionEnvironment())) {
            LOGGER.trace("Environment synchronized {}", event.synchronizedEnvironment);
            refillFeatureBranchesCombo(session.getSessionEnvironment(),
                    session.getSessionFeatureBranchProvider(), true);
            resetDashboardComponents(event.synchronizedEnvironment,
                    session.getSessionFeatureBranch());
            PartHelper.updateLauncherButtonsEnablement(view.getFeatureBranchLauncherButtons());
        }
    }

    /**
     * 
     * @param event
     */
    @Optional
    @Inject
    public void onEnvironmentChange(
            @UIEventTopic(SessionEnvironmentChangeEvent.TOPIC) SessionEnvironmentChangeEvent event) {
        // resets the feature branch combo, to retrieve again the feature branches if
        // not present in case that it is changed, update the view; otherwise, only update if the
        // environment is different from the previous
        if (event.isDifferent()) {
            LOGGER.trace("Environment changed from {} to {}", event.newEnvironment,
                    event.previousEnvironemnt);
            PartHelper.selectEnvironmentOnCombos(session.getSessionEnvironment(),
                    view.getProjectSelectionComboViewer(),
                    view.getEnvironmentSelectionComboViewer());
            refillFeatureBranchesCombo(event.newEnvironment,
                    session.getSessionFeatureBranchProvider(), false);
            resetDashboardComponents(event.newEnvironment, session.getSessionFeatureBranch());
            setButtonEnablement();
        }
    }

    /**
     * 
     * @param event
     */
    @Optional
    @Inject
    public void onFeatureBranchChange(
            @UIEventTopic(SessionFeatureBranchChangeEvent.TOPIC) SessionFeatureBranchChangeEvent event) {
        if (event.isDifferent()) {
            LOGGER.trace("Feature branch changed to {} from {}", event.newFeatureBranch,
                    event.previousFeatureBranch);
            resetDashboardComponents(session.getSessionEnvironment(), event.newFeatureBranch);
            setButtonEnablement();
            recreateLauncherButtons(event.newFeatureBranch);
        }
    }

    @Optional
    @Inject
    private void onRefreshedSession(
            @UIEventTopic(RefreshedObjectEvent.SESSION_CONFIG_REFRESH_TOPIC) final RefreshedObjectEvent<IConfiguration> event) {
        // create the components again and initialize selection
        createComponents();
        initSelection();
        refreshView();
    }

}
