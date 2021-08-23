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
package org.aposin.gem.oomph.impl.internal.config.bean;

import java.util.List;

/**
 * Eclipse Bean for loading the Eclipse configuration.
 */
public class GemEclipseBean {

    public String oomphpath;
    public String productCatalog;
    public String projectCatalog;
    public String bundlePoolConfig;
    public String bundlePool;
    public List<EclipseReleaseBean> releases;

    public String getOomphpath() {
        return oomphpath;
    }

    public void setOomphpath(final String oomphpath) {
        this.oomphpath = oomphpath;
    }

    public String getProductCatalog() {
        return productCatalog;
    }

    public void setProductCatalog(String productCatalog) {
        this.productCatalog = productCatalog;
    }

    public String getProjectCatalog() {
        return projectCatalog;
    }

    public void setProjectCatalog(String projectCatalog) {
        this.projectCatalog = projectCatalog;
    }

    public String getBundlePoolConfig() {
        return bundlePoolConfig;
    }

    public void setBundlePoolConfig(String bundlePoolConfig) {
        this.bundlePoolConfig = bundlePoolConfig;
    }

    public String getBundlePool() {
        return bundlePool;
    }

    public void setBundlePool(String bundlePool) {
        this.bundlePool = bundlePool;
    }

    public List<EclipseReleaseBean> getReleases() {
        return releases;
    }

    public void setReleases(List<EclipseReleaseBean> releases) {
        this.releases = releases;
    }

    /**
     * The Eclipse release Bean.
     */
    public static class EclipseReleaseBean {

        public String name;
        public String displayname;
        // TODO - do we really need a list of paths for each Eclipse?
        // TODO - I will suggest to use just one
        public List<String> paths;
        public List<String> args;
        public List<String> vmargs;

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

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getArgs() {
            return args;
        }

        public void setArgs(List<String> args) {
            this.args = args;
        }

        public List<String> getVmargs() {
            return vmargs;
        }

        public void setVmargs(List<String> vmargs) {
            this.vmargs = vmargs;
        }

    }

}
