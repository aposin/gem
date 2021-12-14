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
package org.aposin.gem.ui.theme;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.theme.icons.IconResources;

/**
 * Constants for the dark theme.
 */
public class ThemeConstants {

    private ThemeConstants() {
        // cannot be instantiated
    }

    /**
     * ID for the default theme.
     */
    public static final String DEFAULT_THEME_ID = "org.aposin.gem.ui.css.theme.default";

    /**
     * ID for the dark theme
     */
    public static final String DARK_THEME_ID = "org.aposin.gem.ui.css.theme.dark";

    /**
     * Bundled stylesheet for SVGs.
     */
    public static final URI SVG_DARKTHEME_STYLESHEET =
            Activator.getResource(IconResources.FILL_WHITE_SVG_CSS);

    /**
     * Stylesheet map for core themes.
     */
    public static final Map<String, URI> STYLESHEET_MAP =
            Collections.singletonMap(DARK_THEME_ID, SVG_DARKTHEME_STYLESHEET);

}
