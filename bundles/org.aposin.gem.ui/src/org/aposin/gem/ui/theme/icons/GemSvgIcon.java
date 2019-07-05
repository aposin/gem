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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;


/**
 * SVG icon implementation for GEM.
 * </br>
 * The icon can contain different styles by themeId and they are registered
 * with the icon styles.
 * The icon is registered in the {@link JFaceResources} registry.
 */
public final class GemSvgIcon extends AbstractGemIcon {

    private final URI iconPath;
    private final URI defaultStyle;
    private final Map<String, URI> iconStyles;

    /**
     * Constructor for an icon without theme support/requirements.
     * 
     * @param iconId
     * @param iconPath
     */
    public GemSvgIcon(final String iconId, final URI iconPath) {
        this(iconId, iconPath, null, Collections.emptyMap());
    }

    /**
     * Constructor for an icon with icon css-styles per-theme.
     * 
     * @param iconId
     * @param iconPath
     * @param defaultStyle default style if none is present; {@code null} if no style.
     * @param iconStyles map of themeId and css-stylesheet for the SVG
     */
    public GemSvgIcon(final String iconId, final URI iconPath, final URI defaultStyle,
            final Map<String, URI> iconStyles) {
        super(iconId);
        this.iconPath = iconPath;
        this.defaultStyle = defaultStyle;
        this.iconStyles = iconStyles;
    }

    @Override
    protected String getRegistryId(final String themeId) {
        if (iconStyles.isEmpty() || !iconStyles.containsKey(themeId)) {
            return getId();
        }
        return themeId + "." + getId();
    }

    @Override
    protected final ImageDescriptor getImageDescriptorForTheme(String themeId, int width,
            int height) {
        try {
            if (iconPath != null) {
                return ImageDescriptor.createFromImageDataProvider(//
                        new SvgImageDataProvider(//
                                iconPath.toURL(), //
                                iconStyles.getOrDefault(themeId, defaultStyle), //
                                width, height));
            }
        } catch (final MalformedURLException e) {
            logger.warn("Missing image file", e);
        }
        return null;
    }

    private static final class SvgImageDataProvider implements ImageDataProvider {

        public final URL svgUrl;
        public final URI styleUri;
        public final float width;
        public final float height;

        public SvgImageDataProvider(final URL svgUrl, final URI styleUri, final int width,
                final int height) {
            this.svgUrl = svgUrl;
            this.styleUri = styleUri;
            this.width = width;
            this.height = height;
        }

        @Override
        public ImageData getImageData(final int zoom) {
            // TODO - should the zoom scale the width and height?
            final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            final TranscoderOutput output = new TranscoderOutput(ostream);
            final PNGTranscoder transcoder = new PNGTranscoder();
            try {
                TranscoderInput input = new TranscoderInput(svgUrl.openStream());
                transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width);
                transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height);
                if (styleUri != null) {
                    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_USER_STYLESHEET_URI,
                            styleUri.toString());
                }
                transcoder.transcode(input, output);
                ostream.flush();
                return new ImageData(new ByteArrayInputStream(ostream.toByteArray()));
            } catch (final IOException | TranscoderException e) {
                return null;
            }
        }
    }

}
