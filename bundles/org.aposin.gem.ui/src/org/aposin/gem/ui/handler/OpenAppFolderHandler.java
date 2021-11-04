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
package org.aposin.gem.ui.handler;

import java.awt.Desktop;
import java.io.IOException;

import org.aposin.gem.core.exception.GemFatalException;
import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.lifecycle.Session;
import org.eclipse.e4.core.di.annotations.CanExecute;

/**
 * Hanlder to open the application folder.
 * 
 * @see Activator#APP_USER_PATH.
 */
public class OpenAppFolderHandler extends GemAbstractSessionHandler {

    @Override
    public void doExecute(Session session) throws Exception {
        try {
            Desktop.getDesktop().open(Activator.APP_USER_PATH.toFile());
        } catch (final IOException e) {
            // convert into a non-fatal GEM exception so the user gets notified instead of doing nothing
            throw new GemFatalException(e.getMessage(), e);
        }
    }

    /**
     * Checks is desktop is supported.
     * @return
     */
    @CanExecute
    public boolean canExecute() {
        return Desktop.isDesktopSupported();
    }

}
