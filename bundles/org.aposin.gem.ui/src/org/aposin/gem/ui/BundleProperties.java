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
package org.aposin.gem.ui;

import java.text.MessageFormat;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.eclipse.e4.core.services.nls.Message;
import org.osgi.framework.Version;

/**
 * Properties from the Bundle. </br>
 * This class captures the properties in the <em>bundle.properties</em>, which
 * are used in the application model too.
 */
// TODO - move BundleProperties to Messages once the Application.e4xmi model is abandoned
@Message
public final class BundleProperties {

    // Application information
    public String application_name;
    public String application_name_short;
    // TODO: does it work as private
    private String application_alpha_marker;
    private String application_name_alpha_format;

    // UI Labels
    public String menuFile_label;
    public String menuFileRefreshConfig_label;
    public String menuFileQuit_label;
    public String menuHelp_label;
    public String menuHelpAbout_label;

    @PostConstruct
    public void format() {
        // add the version to the application names
        final Version version = Activator.getVersion();
        if (Pattern.matches(application_alpha_marker, version.getQualifier())) {
            this.application_name = MessageFormat.format(application_name_alpha_format, application_name);
            this.application_name_short = MessageFormat.format(application_name_alpha_format, application_name_short);
        }
    }
}
