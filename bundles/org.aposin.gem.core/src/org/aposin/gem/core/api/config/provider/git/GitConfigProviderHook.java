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
package org.aposin.gem.core.api.config.provider.git;

import java.text.MessageFormat;

import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.exception.GemFatalException;

/**
 * Configuration hook for several steps in the process of the
 * git configuration.
 * </br>
 * Default implementation fails with a {@link GemConfigurationException}.
 * It could be extended by clients to provide custom behavior (e.g., ask
 * the user for input).
 */
public class GitConfigProviderHook {

    /**
     * Hook to provide custom behaviorwhen a different branch is
     * checked out.
     * 
     * @param configBranch configured branch.
     * @param currentBranch current branch.
     * @return {@code true} if it should checkout; {@code false} if not.
     * @throws GemConfigurationException if it is not allowed to proceed in this case.
     */
    public boolean checkoutWhenDifferentBranch(final String configBranch, final String currentBranch) {
        throw new GemFatalException(MessageFormat.format(//
                "Current branch ({0}) for git-configuration is not the configured one ({1})", //
                currentBranch, configBranch));
    }
    
    /**
     * Hook to check if it should proceed or fail after pull fails.
     * 
     * @param configBranch the configuration branch pulled.
     * @return {@code true} if it should proceed; {@code false} otherwise.
     */
    public boolean proceedIfPullFails(final String configBranch) {
        return false;
    }
    
}
