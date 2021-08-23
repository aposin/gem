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

import org.eclipse.swt.graphics.Image;

/**
 * Interface for icons used by the GEM UI.
 */
public interface IGemIcon {

    /**
     * Default icon size for 100% scale.
     */
    public static final int DEFAULT_ICON_SIZE = 16;

    /**
     * Instance providing no-image.
     */
    public static final IGemIcon NULL_ICON = new IGemIcon() {

        @Override
        public Image getImage(String themeId, int width, int height) {
            return null;
        }

    };

    /**
     * Gets the icon image for the required size.
     *  
     * @param themeId
     * @param width
     * @param height
     * @return
     */
    public Image getImage(final String themeId, final int width, final int height);

    /**
     * Gets the icon image for the default size.
     * 
     * @param themeId
     * @return
     */
    default Image getImage(final String themeId) {
        return getImage(themeId, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE);
    }


}
