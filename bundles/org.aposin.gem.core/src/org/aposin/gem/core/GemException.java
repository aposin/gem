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
package org.aposin.gem.core;

/**
 * Default exception for handled errors in GEM Core. </br>
 * This class is a general purpose exception to catch, but every handled
 * exception in GEM Core should throw a more specific type.
 */
public class GemException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    private final boolean fatal;

    /**
     * Default constructor.
     * 
     * @param msg detail exception message.
     * @param cause underlying cause of the exception (if any).
     * @param fatal {@code true} for a fatal exception; {@code false} otherwise.
     */
    public GemException(final String msg, final Throwable cause, final boolean fatal) {
        super(msg, cause);
        this.fatal = fatal;
    }

    /**
     * Copy constructor to change the fatal status of exception.
     * 
     * @param e exception to copy.
     * @param fatal {@code true} for a fatal exception; {@code false} otherwise.
     */
    public GemException(final GemException e, final boolean fatal) {
        this(e.getMessage(), e.getCause(), fatal);
    }

    /**
     * Creates an exception without cause.
     * 
     * @param msg detail exception message.
     * @param fatal {@code true} for a fatal exception; {@code false} otherwise.
     */
    public GemException(final String msg, final boolean fatal) {
        this(msg, null, fatal);
    }

    /**
     * Creates a non-fatal exception.
     * 
     * @param msg detail exception message.
     * @param cause underlying cause of the exception (if any).
     */
    public GemException(final String msg, final Throwable cause) {
        this(msg, cause, false);
    }

    /**
     * Creates a non-fatal exception without cause.
     * 
     * @param msg detail exception message.
     */
	public GemException(final String msg) {
        this(msg, null, false);
	}

    /**
     * Checks if the exception is fatal.
     * 
     * @return {@code true} if the exception is fatal; {@code false} otherwise.
     */
    public final boolean isFatal() {
        return fatal;
    }

}
