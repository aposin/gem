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
package org.aposin.gem.ui.handler;

import java.text.MessageFormat;

import javax.inject.Inject;

import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.BundleProperties;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Version;

/**
 * Handler to open the About pop-up.
 */
public final class AboutHandler {

    @Inject
    @Translation
    private static BundleProperties bundleProperties;

    @Inject
    @Translation
    public static Messages messages; // NOSONAR

    @Execute
    public void execute(final Shell shell) {
        final Version version = Activator.getVersion();
        // TODO - include NOTICE and LICENSE (maybe with custom dialod?)
        MessageDialog.openInformation(shell, //
                MessageFormat.format(messages.aboutHandler_titleFormat_dialog,
                        bundleProperties.application_name_short), //
                MessageFormat.format(messages.aboutHandler_messageFormat_dialog,
                        bundleProperties.application_name, // param0
                        formatVersion(version), // param1
                        version.getQualifier(), // param2
                        Activator.getVendor())); // param3
    }
    
    private final String formatVersion(Version version) {
        return version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
    }
    
}
