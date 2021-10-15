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
package org.aposin.gem.core.api.config.prefs;

import org.aposin.gem.core.api.INamedObject;

/**
 * Constants related with the configuration core
 * implementation.
 */
public final class PrefConstants {

    /**
     * Preferences ID to identify core preferences config.
     */
    public static final String GEM_PREFERENCES_ID = "gem-prefs";

    /**
     * ID for a preference which represents an object with named
     * binaries.
     */
    public static final String BINARIES_ID = GEM_PREFERENCES_ID + "binaries";

    /**
     * ID for the {@link #getGitBinary()} preference.
     * </br>
     * This forms part of the {@link #BINARIES_ID} path.
     */
    public static final String GIT_ID = BINARIES_ID + ".git";

    /**
     * This group represents a group of system preferences.
     * </br>
     * Extensions can use this group to include system preferences.
     */
    public static final INamedObject SYTEM_GROUP = new INamedObject() {

        @Override
        public String getName() {
            return "sytem";
        }

        @Override
        public String getDisplayName() {
            return "System";
        }
    };

    private PrefConstants() {
        // cannot be instantiated
    }

}
