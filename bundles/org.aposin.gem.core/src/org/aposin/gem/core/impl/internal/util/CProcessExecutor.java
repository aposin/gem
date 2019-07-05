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
package org.aposin.gem.core.impl.internal.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

/**
 * Custom process executor which uses {@link CompletableFuture}
 * instead of {@link Future}.
 * </br>
 * Using this process executor allows to safely cast any {@link Future}
 * to {@link CompletableFuture}, for example from {@link StartedProcess#getFuture()}.
 */
public class CProcessExecutor extends ProcessExecutor {

    /**
     * {@inheritDoc}
     * 
     * @return future of the task as {@link CompletableFuture}
     */
    @Override
    protected <T> Future<T> invokeSubmit(final ExecutorService executor, final Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (final RuntimeException e) {
                // if it is already a runtime exception, rethrow
                throw e;
            } catch (final Exception e) {
                // otherwise, throw a wrapped exception
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
