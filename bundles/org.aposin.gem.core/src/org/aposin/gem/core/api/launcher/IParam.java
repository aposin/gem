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
package org.aposin.gem.core.api.launcher;

import org.aposin.gem.core.api.INamedObject;

/**
 * Interface for parameters for an {@link ILauncher}.
 * </br>
 * Abstract classes <em>must</em> be used to be checked by the
 * presentation code (e.g., UI or CLI):
 * 
 * <ul>
 *  <li>{@link StringParam}</li>
 *  <li>{@link BooleanParam}</lI>
 * </ul>
 * 
 * @param <T> type of the parameter.
 */
// TODO - add more implementations!
public interface IParam<T> extends INamedObject {

    /**
     * Checks if the parameter is required.
     * 
     * @return {@code true} if it is required; {@code false} otherwise.
     */
    public boolean isRequired();

    /**
     * Gets the parameter value.
     * 
     * @return value.
     */
    public T getValue();

    /**
     * Sets the parameter value.
     * 
     * @param value the value.
     */
    public void setValue(final T value);

    /**
     * Validates the value.
     * 
     * @param value the value.
     * @return {@code true} if it is valid; {@code false} otherwise.
     */
    public boolean isValid(T value);

    /**
     * String parameter class.
     */
    public abstract static class StringParam implements IParam<String> {

        private String value = null;

        /**
         * Constructor without initial.
         */
        public StringParam() {
            // NO-OP
        }

        /**
         * Constructor with initial value.
         * 
         * @param value initial value.
         */
        public StringParam(final String value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final String getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(final String value) {
            this.value = value;
        }

    }

    /**
     * Boolean parameter class.
     */
    public abstract static class BooleanParam implements IParam<Boolean> {

        private Boolean value;

        /**
         * Constructor without initial.
         */
        public BooleanParam() {
            // NO-OP
        }

        /**
         * Constructor with initial value.
         * 
         * @param value initial value.
         */
        public BooleanParam(final Boolean value) {
            this.value = value;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public final Boolean getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(final Boolean value) {
            this.value = value;
        }

    }

}
