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
package org.aposin.gem.ui.part.listener;

import org.aposin.gem.core.GemException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * This abstract listener provides the method {@link #selectionChanged(T)} which parameter is of the
 * given type T, offering the new changed element. This listener only supports single selection and
 * throws a {@link GemException} if more than 1 element were selected.
 * 
 * @param <T> the type of element selected
 */
abstract class TypedSingleSelectionChangedListener<T> implements ISelectionChangedListener {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void selectionChanged(SelectionChangedEvent event) {
        final IStructuredSelection selection = event.getStructuredSelection();
        if (!selection.isEmpty()) {
            if (selection.size() == 1) {
                selectionChanged((T) selection.getFirstElement());
            } else {
                throw new IllegalStateException("Multi selection not supported");
            }
        }
    }

    public abstract void selectionChanged(T firstElement);

}
