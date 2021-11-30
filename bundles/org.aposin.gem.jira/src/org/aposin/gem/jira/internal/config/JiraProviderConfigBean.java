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
package org.aposin.gem.jira.internal.config;


public class JiraProviderConfigBean {

    /**
     * The displayName for the JIRA instance.
     */
    public String displayName;

    /**
     * URL for JIRA.
     */
    public String url;

    /**
     * Token for Basic authentication.
     */
    public String authToken;

    /**
     * Jira Query (in JQL).
     */
    // TODO - this JQL might contain some bindings for environment filtering
    // TODO - think about how to handle it (might be useful also for scripting)
    // TODO - or maybe a default JQL and a env_id->JQL
    public String jql;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getJql() {
        return jql;
    }

    public void setJql(String jql) {
        this.jql = jql;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

}
