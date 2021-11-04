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
package org.aposin.gem.ui.process.launcher;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.exception.GemException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * Launcher to copy to the clipboard a supplied attribute.
 */
public final class CopyToClipboardLauncher extends AbstractNoParamsLauncher {

    /**
     * Launcher and launcher-group name.
     */
    public static final String NAME = "copy_to_clipboard";
    
    private static final INamedObject GROUP = new INamedObject() {
        
        @Override
        public String getName() {
            return NAME;
        }
        
        @Override
        public String getDisplayName() {
            return "Copy to clipboard";
        }
    };
    
    private final INamedObject scope;
    private final Supplier<String> attributeProvider;
    private final String attributeName;
    
    /**
     * Default constructor.
     * 
     * @param scope launcher scope
     * @param attributeProvider supplier for the attribute value
     * @param attributeName name for the attribute (for display purposes)
     */
    public CopyToClipboardLauncher(final INamedObject scope, final Supplier<String> attributeProvider, final String attributeName) {
        this.scope = scope;
        this.attributeProvider = attributeProvider;
        this.attributeName = attributeName;
    }
    
    @Override
    public String getId() {
        return NAME + "_" + attributeName.toLowerCase().replace(' ', '_') + scope.getId();
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return attributeName;
    }
    
    @Override
    public INamedObject getGroup() {
        return GROUP;
    }
    
    @Override
    public INamedObject getLaunchScope() {
        return scope;
    }

    @Override
    public boolean canLaunch() {
        return true;
    }

    @Override
    public List<ICommand> launch() throws GemException {
        new Clipboard(null).setContents(//
                new String[] {attributeProvider.get()}, //
                new Transfer[] {TextTransfer.getInstance()});
        return Collections.emptyList();
    }

}
