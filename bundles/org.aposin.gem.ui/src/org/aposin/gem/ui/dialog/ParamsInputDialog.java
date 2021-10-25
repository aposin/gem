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
package org.aposin.gem.ui.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aposin.gem.core.api.launcher.ILauncher;
import org.aposin.gem.core.api.launcher.IParam;
import org.aposin.gem.core.api.launcher.IParam.BooleanParam;
import org.aposin.gem.core.api.launcher.IParam.StringParam;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * Dialog to provide to the user a way to tweak the set of {@link IParam} to
 * use with {@link ILauncher#launch(Set)}.
 * </br>
 * For the {@link StringParam}, the input is copied to the {@link InputDialog}.
 * For the {@link BooleanParam}, the input is a checkbox.
 * </br>
 * TODO: add validation for the parameters.
 */
@SuppressWarnings("rawtypes")
public class ParamsInputDialog extends Dialog {

    private final String title;
    private final Set<IParam> params;
    // TODO - add a map of parameter with its widget
    public final Map<StringParam, Text> stringParams = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param title title for the dialog.
     * @param params parameters to fill in.
     */
    public ParamsInputDialog(final Shell parentShell, final String title,
            final Set<IParam> params) {
        super(parentShell);
        this.title = title;
        this.params = params;
    }

    /**
     * Sets the title.
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    /**
     * Fills the string params with the user input text.
     */
    @Override
    protected void okPressed() {
        // set the text from the input
        for (final Map.Entry<StringParam, Text> entry : stringParams.entrySet()) {
            final String text = entry.getValue().getText();
            if (!text.isEmpty()) {
                entry.getKey().setValue(text);
            }
        }
        super.okPressed();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        for (final IParam p : params) {
            if (p instanceof StringParam) {
                createStringInput(composite, (StringParam) p);
            } else if (p instanceof BooleanParam) {
                createBooleanInput(composite, (BooleanParam) p);
            } else {
                throw new UnsupportedOperationException("Cannot handle param: " + p.getClass());
            }
        }
        applyDialogFont(composite);
        return composite;
    }

    /**
     * Creates the string input in a similar way as in {@link InputDialog}.
     * 
     * @param parent
     * @param param
     */
    private void createStringInput(final Composite parent, final StringParam param) {
        // creates the label with the info
        final Label label = new Label(parent, SWT.WRAP);
        final GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());
        label.setText(param.getDisplayName());

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);

        text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        // TODO - add validation!
        // text.addModifyListener(e -> validateInput());
        // errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        // errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
        // | GridData.HORIZONTAL_ALIGN_FILL));
        // errorMessageText.setBackground(errorMessageText.getDisplay()
        // .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        // // Set the error message text
        // // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
        // setErrorMessage(errorMessage);

        if (param.getValue() != null) {
            text.setText(param.getValue());
        }
        stringParams.put(param, text);
    }

    /**
     * Creates a checkbox for the boolean parameter.
     * 
     * @param parent
     * @param param
     */
    private void createBooleanInput(final Composite parent, final BooleanParam param) {
        final Button button = new Button(parent, SWT.CHECK);
        button.setLayoutData(
                new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        button.setText(param.getDisplayName());
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                param.setValue(button.getSelection());
            }
        });
    }


    /**
     * Get the parameters from the dialog.
     * 
     * @return {@code null} if cancel was pressed; parameters otherwise.
     */
    public Set<IParam> getParams() {
        if (getReturnCode() == CANCEL) {
            return null;
        }
        return params;
    }

}
