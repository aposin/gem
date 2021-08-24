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
package org.aposin.gem.ui.theme.icons;

import java.util.Map;
import org.eclipse.swt.graphics.Image;

/**
 * Icon implementation with a default icon for each theme
 * in case that the icons by theme are not shown.
 */
public final class GemMultiIcon implements IGemIcon {

    private final IGemIcon defaultIcon;
    private final Map<String, IGemIcon> iconsByTheme;
    
    public GemMultiIcon(final IGemIcon defaultIcon, final Map<String, IGemIcon> iconsByTheme) {
        this.defaultIcon = defaultIcon;
        this.iconsByTheme = iconsByTheme;
    }

    @Override
    public Image getImage(final String themeId, final int width, final int height) {
        return iconsByTheme.getOrDefault(themeId, defaultIcon).getImage(themeId, width, height);
    }


}
