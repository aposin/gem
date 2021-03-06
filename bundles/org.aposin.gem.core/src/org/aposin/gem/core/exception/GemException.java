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
package org.aposin.gem.core.exception;

/**
 * Default exception for handled errors in GEM Core. </br>
 * This class is a general purpose exception to catch, but every handled
 * exception in GEM Core should throw a more specific type.
 */
public class GemException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     * 
     * @param msg detail exception message.
     * @param cause underlying cause of the exception (if any).
     */
    public GemException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates an exception without cause.
     * 
     * @param msg detail exception message.
     */
	public GemException(final String msg) {
        this(msg, null);
	}

}
