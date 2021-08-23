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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.lifecycle.event.EnvironmentSynchronizedEvent;
import org.aposin.gem.ui.lifecycle.event.RefreshedObjectEvent;
import org.aposin.gem.ui.lifecycle.event.SessionEnvironmentChangeEvent;
import org.aposin.gem.ui.message.MessageRegistry;
import org.aposin.gem.ui.part.listener.EnvironmentSelectionListener;
import org.aposin.gem.ui.part.listener.ProjectSelectionListener;
import org.aposin.gem.ui.part.listener.dashboard.DashboardDynamicButtonListener;
import org.aposin.gem.ui.part.listener.dashboard.LauncherDynamicMenuListener;
import org.aposin.gem.ui.part.listener.workflow.EnvironmentWorkflowOnSelectionListener;
import org.aposin.gem.ui.process.CallbackRunnable;
import org.aposin.gem.ui.process.LocalRepoStatus;
import org.aposin.gem.ui.process.launcher.SwitchLauncher;
import org.aposin.gem.ui.view.EnvironmentWorkflowView;
import org.aposin.gem.ui.view.dashboard.EnvironmentDashboardInfo;
import org.aposin.gem.ui.view.dashboard.RepositoryDashboardInfoContainer;
import org.aposin.gem.ui.view.labelprovider.TypedColumnLabelProvider.TypedColumnLabelProviderFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class EnvironmentSetupPart {

    private EnvironmentWorkflowView view;
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
    public void postConstruct(final Composite parent) {
        view = new EnvironmentWorkflowView(parent, SWT.NONE);
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
    }

    private void initSelection() {
        PartHelper.selectEnvironmentOnCombos(session.getSessionEnvironment(),
                view.getProjectSelectionComboViewer(), view.getEnvironmentSelectionComboViewer());
    }

    private void refreshView() {
        updateEnvironmentComponents(session.getSessionEnvironment());
    }

    private void createComboViewers() {
        PartHelper.setEnvironmentInformationData(session, view.getProjectSelectionComboViewer(),
                view.getEnvironmentSelectionComboViewer());
    }

    private void setupTableViewer() {
        TypedColumnLabelProviderFactory.create(EnvironmentDashboardInfo.class) //
                .text(EnvironmentDashboardInfo::getRepositoryName) //
                .image(info -> info.getStatus().getStatusDecoratorImage()) //
                .set(view.getRepoNameViewerColumn());
        TypedColumnLabelProviderFactory.create(EnvironmentDashboardInfo.class) //
                .text(RepositoryDashboardInfoContainer::getBaseBranch) //
                .set(view.getBaseBranchViewerColumn());
        TypedColumnLabelProviderFactory.create(EnvironmentDashboardInfo.class) //
                .text(info -> info.getStatus().getStatusString(registry.getMessages())) //
                .set(view.getRepoStatusViewerColumn());
        TypedColumnLabelProviderFactory.create(EnvironmentDashboardInfo.class) //
                .text(EnvironmentDashboardInfo::getWorktreeLocation) //
                .set(view.getRepoWorktreeLocationViewerColumn());
    }

    private void registerListeners() {
        // TODO - should this be moved to the part helper?
        view.getEnvironmentSelectionComboViewer()
                .addSelectionChangedListener(new EnvironmentSelectionListener(session));
        view.getProjectSelectionComboViewer().addSelectionChangedListener(
                new ProjectSelectionListener(session, view.getEnvironmentSelectionComboViewer()));

        // button listeners for the workflow
        PartHelper.addButtonLauncherListener(view.getCloneRepositoriesButton(), //
                new EnvironmentWorkflowOnSelectionListener(session, env -> env.getWorkflow() //
                        .getCloneLauncher()));
        PartHelper.addButtonLauncherListener(view.getSynchronizeAllEnvBranchesButton(),
                new EnvironmentWorkflowOnSelectionListener(session, env -> env.getWorkflow()//
                        .getSynchronizeAllEnvBranchesLauncher()));
        PartHelper.addButtonLauncherListener(view.getSetupWorktreeButton(), //
                new EnvironmentWorkflowOnSelectionListener(session, env -> env.getWorkflow() //
                        .getSetupWorktreeLauncher()));
        PartHelper.addButtonLauncherListener(view.getRemoveWorktreeButton(),
                new EnvironmentWorkflowOnSelectionListener(session, //
                        env -> new SwitchLauncher(env.getWorkflow().getRemoveWorktreeLauncher(), //
                            () -> MessageDialog.openConfirm(null, //
                                    registry.getMessages().workflowRemoveWorktree_label_common, //
                                        MessageFormat.format(// 
                                                registry.getMessages().workflowRemoveWorktree_warningMessageFormat_common, //
                                                env.getProject().getDisplayName(), env.getDisplayName())))
                ));
        // also register the context menu and the dashboard buttons
        LauncherDynamicMenuListener.registerListener(view.getDashboardView().getTableViewer().getTable());
        dashboardListener = DashboardDynamicButtonListener.registerListener(//
                view.getDashboardView(), registry, themeEngine);
    }

    private void updateEnvironmentComponents(final IEnvironment environment) {
        // update te repo info for the table
        final List<RepositoryDashboardInfoContainer> tableInfo = environment.getEnvironmentWorktrees().stream() //
                .map(repo -> toRepoTableInfoWithCallback(repo, environment)) //
                .collect(Collectors.toList());
        
        PartHelper.setDashboardContent(view.getDashboardView(), tableInfo);
        // update the launcher-based buttons
        PartHelper.setLauncherButtonEnabled(view.getCloneRepositoriesButton());
        PartHelper.setLauncherButtonEnabled(view.getSetupWorktreeButton());
        PartHelper.setLauncherButtonEnabled(view.getRemoveWorktreeButton());
        PartHelper.setLauncherButtonEnabled(view.getSynchronizeAllEnvBranchesButton());
        PartHelper.updateLauncherButtonsEnablement(view.getByRepositoryLauncherButtons());
    }

    private EnvironmentDashboardInfo toRepoTableInfoWithCallback(final IWorktreeDefinition worktreeDef,
            final IEnvironment environment) {
        final EnvironmentDashboardInfo info = new EnvironmentDashboardInfo(environment,
                environment.getEnvironmentWorktrees().indexOf(worktreeDef));
        final CallbackRunnable<LocalRepoStatus> runnable = new CallbackRunnable<LocalRepoStatus>() {

            @Override
            public LocalRepoStatus doRun() {
                return info.computeStatus();
            }

            @Override
            public void callback(final LocalRepoStatus value) {
                if (value != null) {
                    // TODO - could we refresh only the current row?
                    view.getDashboardView().getTableViewer().refresh();
                }
            }
        };

        // run it on the service
        uiSynchronize.asyncExec(runnable);

        return info;

    }
    
    @Optional
    @Inject
    private void onThemeChanged(@UIEventTopic(IThemeEngine.Events.THEME_CHANGED) Object obj) {
        dashboardListener.trigger();
    }

    @Optional
    @Inject
    private void onEnvironmentChange(
            @UIEventTopic(SessionEnvironmentChangeEvent.TOPIC) final SessionEnvironmentChangeEvent event) {
        if (event.isDifferent()) {
            PartHelper.selectEnvironmentOnCombos(event.newEnvironment,
                    view.getProjectSelectionComboViewer(),
                    view.getEnvironmentSelectionComboViewer());
            updateEnvironmentComponents(event.newEnvironment);
        }
    }

    @Optional
    @Inject
    private void onEnvironmentSynchronized(
            @UIEventTopic(EnvironmentSynchronizedEvent.TOPIC) final EnvironmentSynchronizedEvent event) {
        if (Objects.equals(event.synchronizedEnvironment, session.getSessionEnvironment())) {
            updateEnvironmentComponents(event.synchronizedEnvironment);
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
