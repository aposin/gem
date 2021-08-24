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
package org.aposin.gem.logging.e4;

import org.eclipse.e4.core.services.log.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for an slf4j logger. 
 */
public class SLF4JLogger extends Logger {

    private final org.slf4j.Logger original;

    public SLF4JLogger(org.slf4j.Logger original) {
        this.original = original;
    }

    /* package */ SLF4JLogger(final Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    @Override
    public boolean isErrorEnabled() {
        return original.isErrorEnabled();
    }

    @Override
    public void error(Throwable t, String message) {
        original.error(message, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return original.isWarnEnabled();
    }

    @Override
    public void warn(Throwable t, String message) {
        original.warn(message, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return original.isInfoEnabled();
    }

    @Override
    public void info(Throwable t, String message) {
        original.info(message, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return original.isTraceEnabled();
    }

    @Override
    public void trace(Throwable t, String message) {
        original.trace(message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return original.isDebugEnabled();
    }

    @Override
    public void debug(Throwable t) {
        original.debug(null, t);
    }

    @Override
    public void debug(Throwable t, String message) {
        original.debug(message, t);
    }

}
