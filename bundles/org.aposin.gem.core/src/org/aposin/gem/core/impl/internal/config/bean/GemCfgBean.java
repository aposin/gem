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
package org.aposin.gem.core.impl.internal.config.bean;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.aposin.gem.core.api.model.repo.GemRepoHookDescriptor;
import com.typesafe.config.Optional;

/**
 * GEM Configuration definition as a Java Bean.
 */
// TODO - create JSON schema and generate with a maven Plug-in
public final class GemCfgBean {

    public String resourcesdirectory;
    public String defaultfeaturebranchprovider;
    @Optional
    public String manualbranchid = System.getProperty("user.name");
    // TODO - this should be a map id=url (both as Strings), but the Config library
    // TODO - does not handle typed maps
    public Set<RepositoryBean> repositories;
    public Set<ProjectBean> projects;

    public String getResourcesdirectory() {
        return resourcesdirectory;
    }

    public void setResourcesdirectory(String resourcesdirectory) {
        this.resourcesdirectory = resourcesdirectory;
    }

    public String getManualBranchId() {
        return manualbranchid;
    }

    public void setManualbranchid(final String manualbranchid) {
        this.manualbranchid = manualbranchid;
    }

    public String getDefaultfeaturebranchprovider() {
        return defaultfeaturebranchprovider;
    }

    public void setDefaultfeaturebranchprovider(String defaultfeaturebranchprovider) {
        this.defaultfeaturebranchprovider = defaultfeaturebranchprovider;
    }

    public Set<RepositoryBean> getRepositories() {
        return repositories;
    }

    public void setRepositories(Set<RepositoryBean> repositories) {
        this.repositories = repositories;
    }

    public Set<ProjectBean> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectBean> projects) {
        this.projects = projects;
    }

    public static class RepositoryBean {

        public String id;
        public String url;
        public String server;

        @Optional
        public Set<RepoHookBean> hooks = Collections.emptySet();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public Set<RepoHookBean> getHooks() {
            return hooks;
        }

        public void setHooks(Set<RepoHookBean> githooks) {
            this.hooks = githooks;
        }

        @Override
        public String toString() {
            return "RepositoryBean [id=" + id + ", url=" + url + ", server=" + server + "]";
        }
    }

    public static class ProjectBean {
        public String name;
        public String displayname;
        public Set<EnvironmentBean> environments;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayname() {
            return displayname;
        }

        public void setDisplayname(String displayname) {
            this.displayname = displayname;
        }

        public Set<EnvironmentBean> getEnvironments() {
            return environments;
        }

        public void setEnvironments(Set<EnvironmentBean> environments) {
            this.environments = environments;
        }

        @Override
        public String toString() {
            return "ProjectBean [name=" + name + ", displayname=" + displayname + ", environments="
                    + environments + "]";
        }
    }

    public static class EnvironmentBean {
        public String name;
        public String displayname;
        // TODO - this should be a map of repoId=branch, but the library does not
        // handled maps yet
        public Map<String, Object> branches;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayname() {
            return displayname;
        }

        public void setDisplayname(String displayname) {
            this.displayname = displayname;
        }

        public Map<String, Object> getBranches() {
            return branches;
        }

        public void setBranches(Map<String, Object> branches) {
            this.branches = branches;
        }

        @Override
        public String toString() {
            return "EnvironmentBean [name=" + name + ", displayname=" + displayname + ", branches="
                    + branches + "]";
        }
    }

    public static class RepoHookBean {
        public String path;
        public Set<GemRepoHookDescriptor.InstallScope> scopes;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Set<GemRepoHookDescriptor.InstallScope> getScopes() {
            return scopes;
        }

        public void setScopes(Set<GemRepoHookDescriptor.InstallScope> scopes) {
            this.scopes = scopes;
        }

    }

    @Override
    public String toString() {
        return "GemCfgBean [resourcesdirectory=" + resourcesdirectory + ", repositories="
                + repositories + ", projects=" + projects + "]";
    }

}
