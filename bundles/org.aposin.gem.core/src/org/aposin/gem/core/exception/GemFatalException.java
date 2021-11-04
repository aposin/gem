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
 * GEM exception for a fatal error.
 */
public final class GemFatalException extends GemException {

    /**
     * Default constructor.
     * 
     * @param msg detail exception message.
     * @param cause underlying cause of the exception (if any).
     */
    public GemFatalException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates an exception without cause.
     * 
     * @param msg detail exception message.
     */
    public GemFatalException(String msg) {
        super(msg);
    }

    public static GemFatalException from(final GemException e) {
        return new GemFatalException(e.getLocalizedMessage(), e.getCause());
    }

}
