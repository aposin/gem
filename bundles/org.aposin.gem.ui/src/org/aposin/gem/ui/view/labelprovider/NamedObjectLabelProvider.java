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
package org.aposin.gem.ui.view.labelprovider;

import org.aposin.gem.core.api.INamedObject;
import org.eclipse.jface.viewers.LabelProvider;

public class NamedObjectLabelProvider extends LabelProvider {

    private final Class<? extends INamedObject> clazz;

    /**
     * Constructor to restrict the class that will be evaluated.
     * 
     * @param clazz class to check instance type.
     */
    public NamedObjectLabelProvider(Class<? extends INamedObject> clazz) {
        this.clazz = clazz;
    }

    /**
     * Default constructor.
     */
    public NamedObjectLabelProvider() {
        this(INamedObject.class);
    }

    @Override
    public String getText(Object element) {
        if (clazz.isInstance(element)) {
            return ((INamedObject) element).getDisplayName();
        }
        return super.getText(element);
    }

}
