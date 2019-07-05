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
package org.aposin.gem.ui.part;

import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.ui.lifecycle.Session;
import org.aposin.gem.ui.lifecycle.event.EnvironmentSynchronizedEvent;
import org.aposin.gem.ui.lifecycle.event.RefreshedObjectEvent;
import org.aposin.gem.ui.lifecycle.event.SessionEnvironmentChangeEvent;
import org.aposin.gem.ui.view.DynamicButtonGroupListView;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * This part contains and manages the starter buttons running applications and launchers.
 */
public class EnvironmentLauncherPart {

    @Inject
    private IThemeEngine themeEngine;

    @Inject
    private Session session;
    private DynamicButtonGroupListView view;

    private final MPart part;
    private final MPartSashContainer parent;
    private final MPartSashContainerElement sibling;

    @Inject
    public EnvironmentLauncherPart(MPart part) {
        this.part = part;
        MPartSashContainer sash = null;
        MPartSashContainerElement siblingElement = null;
        final MElementContainer<?> parentContainer = part.getParent();
        if (parentContainer instanceof MPartSashContainer) {
            sash = (MPartSashContainer) parentContainer;
            final List<MPartSashContainerElement> children = sash.getChildren();
            if (children.size() == 2) {
                siblingElement = children.get(0) == part ? children.get(1) : children.get(0);
            }
        }
        this.parent = sash;
        this.sibling = siblingElement;
    }

    @PostConstruct
    public void postConstruct(final Composite parent) {
        this.view = new DynamicButtonGroupListView(parent, SWT.HORIZONTAL);
        createLauncherButtons(session.getSessionEnvironment());
        PartHelper.updateLauncherButtonsEnablement(view);
        if (parent != null && sibling != null) {
            this.view.addListener(SWT.Resize, e -> recalculateSashContainer());
        }
    }

    private void createLauncherButtons(final IEnvironment environment) {
        PartHelper.recreateLauncherButtons(view, environment.getLaunchers(), PartHelper.getActiveTheme(themeEngine));
    }

    @Optional
    @Inject
    private void onThemeChanged(@UIEventTopic(IThemeEngine.Events.THEME_CHANGED) Object obj) {
        createLauncherButtons(session.getSessionEnvironment());
    }

    @Optional
    @Inject
    private void onEnvironmentChange(
            @UIEventTopic(SessionEnvironmentChangeEvent.TOPIC) final SessionEnvironmentChangeEvent event) {
        if (event.isDifferent()) {
            createLauncherButtons(event.newEnvironment);
        }
    }

    @Optional
    @Inject
    private void onEnvironmentSynchronized(
            @UIEventTopic(EnvironmentSynchronizedEvent.TOPIC) final EnvironmentSynchronizedEvent event) {
        if (Objects.equals(event.synchronizedEnvironment, session.getSessionEnvironment())) {
            PartHelper.updateLauncherButtonsEnablement(view);
        }
    }

    @Optional
    @Inject
    private void onRefreshedSession(
            @UIEventTopic(RefreshedObjectEvent.SESSION_CONFIG_REFRESH_TOPIC) final RefreshedObjectEvent<IConfiguration> event) {
        // create the components again and initialize selection
        createLauncherButtons(session.getSessionEnvironment());
    }

    /**
     * Recalculates the weights of the sash container to guarantee that this part always gets enough
     * space to be shown vertically.
     */
    private void recalculateSashContainer() {
        final Rectangle bounds = view.getBounds();
        final Point requiredBounds = view.computeSize(bounds.width, SWT.DEFAULT);
        if (requiredBounds.y > bounds.height) {
            final Composite sashControl = (Composite) parent.getWidget();
            final int sashHeight = sashControl.getBounds().height;
            sibling.setContainerData(Integer.toString(sashHeight - requiredBounds.y));
            part.setContainerData(Integer.toString(requiredBounds.y));
            try {
                sashControl.setRedraw(false);
                sashControl.layout();
            } finally {
                sashControl.setRedraw(true);
            }
            sashControl.update();
        }
    }

}
