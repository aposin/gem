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
package org.aposin.gem.ui.theme.icons;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;

/**
 * GEM icon implementation using default SWT {@link ImageDescriptor} from URL.
 */
public final class GemSwtIcon extends AbstractGemIcon {

    private final URI iconPath;

    /**
     * Constructor for a SWT icon.
     * 
     * @param iconId
     * @param defaultIconPath icon path for no theme/default theme.
     */
    public GemSwtIcon(final String iconId, final URI iconPath) {
        super(iconId);
        this.iconPath = iconPath;
    }

    @Override
    protected String getRegistryId(final String themeId) {
        return getId();
    }

    @Override
    protected ImageDescriptor getImageDescriptorForTheme(final String themeId, final int width,
            final int height) {
        try {
            final URL imageUrl = iconPath.toURL();
            final ImageDataProvider provider = zoom -> {
                final ImageData imageData =
                        ImageDescriptor.createFromURL(imageUrl).getImageData(zoom);
                // TODO - should this check use the zoom?
                if (imageData.width == width && imageData.height == height) {
                    return imageData;
                }
                return imageData.scaledTo(width, height);
            };
            return ImageDescriptor.createFromImageDataProvider(provider);
        } catch (final MalformedURLException e) {
            logger.warn("Missing image file", e);
        }
        return null;
    }

}
