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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of GEM icon based on {@link ImageDescriptor}
 * and cached in {@link JFaceResources} registry.
 */
abstract class AbstractGemIcon implements IGemIcon {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String iconId;

    protected AbstractGemIcon(final String iconId) {
        this.iconId = iconId;
    }

    /**
     * Gets the image descriptor for the theme and size.
     * 
     * @param themeId
     * @param width
     * @param height
     * @return
     */
    protected abstract ImageDescriptor getImageDescriptorForTheme(final String themeId,
            final int width, final int height);

    /**
     * Gets the ID for the registry, based on the theme ID.
     * @param themeId
     * @return
     */
    protected abstract String getRegistryId(final String themeId);

    protected final String getId() {
        return iconId;
    }

    @Override
    public final Image getImage(final String themeId, final int width, final int height) {
        final String iconKey = String.format("%s@%sx%s", getRegistryId(themeId), width, height);
        Image image = JFaceResources.getImage(iconKey);
        if (image == null) {
            final ImageDescriptor descriptor = getImageDescriptorForTheme(themeId, width, height);
            if (descriptor != null) {
                JFaceResources.getImageRegistry().put(iconKey, descriptor);
                image = JFaceResources.getImage(iconKey);
            }
        }
        return image;
    }

}
