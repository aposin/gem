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
package org.aposin.gem.ui.dialog.progress.internal;

import java.util.Arrays;
import java.util.List;

import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CliProgressMonitorDialog extends ProgressMonitorDialog {

    // init the console font
    private static final String CONSOLE_FONT_KEY = "CliProgressMonitorDialog_font";
    private static final String CONSOLE_FONT_NAME = "Lucida Console";
    static {
        final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
        final int[] heights = Arrays.stream(fontRegistry.defaultFont().getFontData()) //
                .mapToInt(FontData::getHeight)//
                .toArray();
        final FontData[] consoleFontData = new FontData[heights.length];
        for (int idx = 0; idx < heights.length; idx++) {
            consoleFontData[idx] = new FontData(CONSOLE_FONT_NAME, heights[idx], SWT.NONE);
        }
        fontRegistry.put(CONSOLE_FONT_KEY, consoleFontData);
    }

    private final List<ICommand> commands;
    private final Messages messages;
    // required to clean cursors
    private Button commandDetailsButton;
    private Cursor commandDetailsButtonCursor;

    public CliProgressMonitorDialog(final Shell parent, final Messages messages, final List<ICommand> commands) {
        super(parent);
        this.messages = messages;
        this.commands = commands;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        final Button showErrorsButton =
                createButton(parent, IDialogConstants.DETAILS_ID, messages.cliProgressMonitorDialog_label_detailsButton,
                        false);
        showErrorsButton.setEnabled(false);
        final Button okButton = createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, false);
        okButton.setEnabled(false);
    }

    public Button getShowErrorsButton() {
        return getButton(IDialogConstants.DETAILS_ID);
    }

    @Override
    protected void finishedRun() {
        decrementNestingDepth();
        clearCursors();
        getButton(IDialogConstants.CLOSE_ID).setEnabled(true);
    }

    @Override
    protected void buttonPressed(final int buttonId) {
        if (buttonId == IDialogConstants.CLOSE_ID) {
            close();
            return;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void clearCursors() {
        if (commandDetailsButton != null) {
            commandDetailsButton.setCursor(null);
        }
        if (commandDetailsButtonCursor != null) {
            commandDetailsButtonCursor.dispose();
        }
        super.clearCursors();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite createDialogArea = (Composite) super.createDialogArea(parent);
        createCommandDetailsButton(parent);
        final Composite cmdTextCompositeParent = new Composite(parent, SWT.NONE);
        cmdTextCompositeParent.setLayout(new GridLayout(2, false));
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).exclude(true).applyTo(cmdTextCompositeParent);
        addToggleListener(cmdTextCompositeParent);
        for (final ICommand cmd : commands) {
            new Text(cmdTextCompositeParent, SWT.BORDER).setText(cmd.getCommandScope().getDisplayName());
            createCommandLineStyledText(cmdTextCompositeParent, cmd);
        }

        return createDialogArea;
    }

    private void createCommandDetailsButton(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.VERTICAL));

        GridDataFactory.fillDefaults().span(2, 1).applyTo(composite);
        commandDetailsButton = new Button(composite, SWT.TOGGLE);
        commandDetailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        // create cursor
        final Cursor buttonCursor = new Cursor(commandDetailsButton.getDisplay(), SWT.CURSOR_ARROW);
        commandDetailsButton.setCursor(buttonCursor);
    }

    private void addToggleListener(final Composite toggledComposite) {
        // add button listener to show the command line text
        commandDetailsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final boolean selection = commandDetailsButton.getSelection();
                // set visibility on the text
                toggledComposite.setVisible(selection);
                // exclude the parent from computing size (if not selected)
                ((GridData) toggledComposite.getLayoutData()).exclude = !selection;
                if (selection) {
                    commandDetailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
                } else {
                    commandDetailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
                }
                // resize shell to show!
                final Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
                getShell().setSize(size);
                getShell().layout(true, true);
            }

        });
    
    }

    private static StyledText createCommandLineStyledText(final Composite parent,
            final ICommand command) {
        final StyledText styledText = new StyledText(parent,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().grab(true, true).hint(0, 100).span(2, 1).applyTo(styledText);
        styledText.setBackground(styledText.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        styledText.setForeground(styledText.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        styledText.setFont(JFaceResources.getFont(CONSOLE_FONT_KEY));

        // also register an output stream for the text component and add to the command
        command.addStdOutStream(new StyledTextControlOutputStream(styledText));
        command.addStdErrStream(new StyledTextControlOutputStream(styledText,
                styledText.getDisplay().getSystemColor(SWT.COLOR_RED)));

        return styledText;
    }

}
