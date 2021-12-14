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

import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Image;

/**
 * This implementation of {@link ColumnLabelProvider} provides typed methods for
 * {@link #getText(Object)} and {@link #getImage(Object)}, so cast checks and
 * casts are not necessary anymore on all implementations of
 * {@link ColumnLabelProvider}. Implementors of this class should overwrite
 * {@link #getTypedText(Object)} and {@link #getTypedImage(Object)} to define
 * text and image definitions of viewer columns.
 * 
 * @param <T> the type of element to deal with
 */
@SuppressWarnings("unchecked")
public class TypedColumnLabelProvider<T> extends ColumnLabelProvider {

    private final boolean ignoreUntyped;
    private final Class<T> type;

    /**
     * @param type the type of element to deal with
     */
    public TypedColumnLabelProvider(Class<T> type) {
        this(type, false);
    }

    public TypedColumnLabelProvider(Class<T> type, boolean ignoreUntyped) {
        this.type = type;
        this.ignoreUntyped = ignoreUntyped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(Object element) {
        if (type.isInstance(element)) {
            return getTypedText((T) element);
        }
        return ignoreUntyped ? null : super.getText(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(Object element) {
        if (type.isInstance(element)) {
            return getTypedImage((T) element);
        }
        return ignoreUntyped ? null : super.getImage(element);
    }

    /**
     * Hook method, which intended to be overwritten, providing the object with it's
     * correct type.
     * 
     * @param object the object
     * @return the String to show
     */
    protected String getTypedText(T object) {
        return super.getText(object);
    }

    /**
     * Hook method, which intended to be overwritten, providing the object with it's
     * correct type.
     * 
     * @param object the object
     * @return the image to show
     */
    protected Image getTypedImage(T object) {
        return super.getImage(object);
    }

    /**
     * This factory creates a {@link TypedColumnLabelProvider} and applies settings
     * to the {@link ViewerColumn} by using {@link #set(ViewerColumn)}.
     * 
     * @param <T> the type of element to deal with
     */
    public static class TypedColumnLabelProviderFactory<T> {

        private final Class<T> type;
        private Function<T, String> text;
        private Function<T, Image> image;
        private boolean ignoreUntyped = false;

        /**
         * @param type the type of element to deal with
         */
        private TypedColumnLabelProviderFactory(Class<T> type) {
            this.type = type;
        }

        /**
         * Creates a new instance of factory.
         * 
         * @param <T>  the type of element to deal with
         * @param type the class of element to deal with
         * @return
         */
        public static <T> TypedColumnLabelProviderFactory<T> create(Class<T> type) {
            return new TypedColumnLabelProviderFactory<>(type);
        }

        /**
         * Ignores the untyped objects set on the column.
         * 
         * @param ignoreUntyped {@code true} to ignore; {@code false} otherwise.
         * 
         * @return the factory.
         */
        public TypedColumnLabelProviderFactory<T> ignoreUntyped(final boolean ignoreUntyped) {
            this.ignoreUntyped = ignoreUntyped;
            return this;
        }

        /**
         * Appends a text function which gets executed in
         * {@link TypedColumnLabelProvider#getTypedText(Object)}.
         * 
         * @param text the function for providing the text
         * @return the factory
         */
        public TypedColumnLabelProviderFactory<T> text(Function<T, String> text) {
            this.text = text;
            return this;
        }

        /**
         * Appends an image function which gets executed in
         * {@link TypedColumnLabelProvider#getTypedImage(Object)}.
         * 
         * @param text the function for providing the image
         * @return the factory
         */
        public TypedColumnLabelProviderFactory<T> image(Function<T, Image> image) {
            this.image = image;
            return this;
        }

        /**
         * Creates and sets a new instance of {@link TypedColumnLabelProvider} into the
         * given {@link ViewerColumn} as label provider.
         * 
         * @param column the column where to set the label provider
         */
        public void set(ViewerColumn column) {
            column.setLabelProvider(new TypedColumnLabelProvider<>(type, ignoreUntyped) {

                @Override
                protected String getTypedText(T object) {
                    if (text != null) {
                        return text.apply(object);
                    } else {
                        return super.getTypedText(object);
                    }
                }

                @Override
                protected Image getTypedImage(T object) {
                    if (image != null) {
                        return image.apply(object);
                    } else {
                        return super.getTypedImage(object);
                    }
                }

            });
        }

    }

}
