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
package org.aposin.gem.ui.view;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class DynamicButtonGroup extends Composite {

    private Group buttonGroup;
    private final List<Button> buttons;

    public DynamicButtonGroup(final Composite parent, final int style) {
        super(parent, style);
        RowLayoutFactory.fillDefaults().applyTo(this);
        buttonGroup = new Group(this, SWT.NONE);
        RowLayoutFactory.swtDefaults().applyTo(buttonGroup);
        buttons = new ArrayList<>();
    }

    /**
     * Creates a new button on the composite.
     * </br>
     * Note that this does not layout the composite.
     * {@link #layout()} should be called after adding new buttons.
     * 
     * @param style button style.
     * @return created button.
     */
    public Button createButton(final int style) {
        final Button button = new Button(buttonGroup, style);
        buttons.add(button);
        return button;
    }

    public void setText(final String text) {
        buttonGroup.setText(text);
    }

    /**
     * Gets the button list.
     * 
     * @return button list.
     */
    public List<Button> getButtons() {
        return buttons;
    }
}
