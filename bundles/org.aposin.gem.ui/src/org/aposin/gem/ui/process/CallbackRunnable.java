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
package org.aposin.gem.ui.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CallbackRunnable<V> implements Runnable {

    /**
     * Logger for the class.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Runs the operation and, independently of any exception,
     * the callback.
     */
    @Override
    public final void run() {
        V result = null;
        try {
            result = doRun();
        } catch (final Exception e) {
            handleException(e);
        } finally {
            callback(result);
        }
    }

    /**
     * Override to provide a way to handle an exception.
     * </br>
     * Default logs to error.
     */
    public void handleException(final Exception e) {
        logger.error(e.getLocalizedMessage(), e);
    }

    /**
     * Do the work and return the value to use on the callback.
     * 
     * @return value to use on the callback.
     */
    public abstract V doRun();

    /**
     * Callback used by a service to use the value set on run.
     * 
     * @param value value returned by {@link #doRun()}; {@code null} if failure.
     */
    public abstract void callback(V value);

}
