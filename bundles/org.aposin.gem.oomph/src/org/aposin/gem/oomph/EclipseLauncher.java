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
package org.aposin.gem.oomph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.IConfigurable;
import org.aposin.gem.core.api.config.IConfiguration;
import org.aposin.gem.core.api.launcher.AbstractNoParamsLauncher;
import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.IEnvironmentWorkflow;
import org.aposin.gem.core.exception.GemException;
import org.aposin.gem.core.utils.ExecUtils;
import org.aposin.gem.oomph.impl.internal.config.bean.GemEclipseBean;
import org.aposin.gem.oomph.impl.internal.config.bean.GemEclipseBean.EclipseReleaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Launcher to start and setup an Eclipse workspace with an already installed product and oomph configuration.
 */
// TODO #41 - refactor to use the oomph libraries
public class EclipseLauncher extends AbstractNoParamsLauncher implements IConfigurable {

    public static final String LAUNCHER_NAME = "eclipse";

    private static final Logger LOGGER = LoggerFactory.getLogger(EclipseLauncher.class);

    private final EclipseLauncherProvider provider;
    private final IEnvironment environment;
    private final int index;

    public EclipseLauncher(final EclipseLauncherProvider provider, final IEnvironment environment,
            final int index) {
        this.provider = provider;
        this.environment = environment;
        this.index = index;
    }

    @Override
    public INamedObject getGroup() {
        return provider;
    }

    @Override
    public IEnvironment getLaunchScope() {
        return environment;
    }

    @Override
    public IConfiguration getConfiguration() {
        return getLaunchScope().getConfiguration();
    }

    private GemEclipseBean getEclipseBean() {
        return provider.eclipseBean;
    }

    private EclipseReleaseBean getEclipseReleaseBean() {
        return getEclipseBean().releases.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return getEclipseReleaseBean().displayname;
    }

    @Override
    public String getId() {
        return getName() + '_' + getEclipseReleaseBean() + '_' + getLaunchScope().getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return LAUNCHER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canLaunch() {
        final IEnvironmentWorkflow workflow = getLaunchScope().getWorkflow();
        if (!(workflow.getCloneLauncher().canLaunch() || workflow.getSetupWorktreeLauncher().canLaunch())) {
            return findFirsExistingEclipseFolder() != null;
        }
        return false;
    }

    /**
     * Gets the first existing eclipse configured.
     * 
     * @return existing eclipse path; {@code null} otherwise
     */
    private final Path findFirsExistingEclipseFolder() {
        return getEclipseReleaseBean().paths.stream()//
                .map(Paths::get) // convert to path
                .filter(Files::exists) // filter the ones existing
                .findFirst().orElseGet(() -> null);
    }

    /**
     * Starts Eclipse as application on the environment.
     * 
     * @see https://wiki.eclipse.org/Equinox_Launcher
     * @throws GemException if there is any problem starting the environment workspace
     */
    @Override
    public List<ICommand> launch() throws GemException {
        final Path eclipseFolder = findFirsExistingEclipseFolder();
        if (eclipseFolder == null) {
            LOGGER.error("Could not start Eclipse.");
            throw new GemException("Misconfigured eclipse");
        }

        final Path projectEnvironment = getConfiguration().getResourcesDirectory() //
                .resolve("eclipseworkspaces") //
                .resolve(getLaunchScope().getId());

        setBundlePool();
        createEclipseOomphSetup(projectEnvironment);
        createWorkspaceOomphSetup(projectEnvironment);

        try {
            LOGGER.trace("About to run eclipse command");
            final List<String> cmd = createCmd(eclipseFolder, projectEnvironment);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Eclipse command: '{}'", cmd.stream().collect(Collectors.joining(" ")));
            }
            ExecUtils.exec(cmd, eclipseFolder);
        } catch (final IOException e) {
            LOGGER.error("Could not start Eclipse.", e);
            throw new GemException("Could not start eclipse.", e);
        }

        // empty list - not in parallel or trackable
        return Collections.emptyList();
    }

    /**
     * Creates the command line array to start the Eclipse for the given project environment.
     * 
     * @param projectEnvironment the project environment
     * @return the command line list to start the Eclipse.
     */
    private List<String> createCmd(final Path eclipseFolder, final Path projectEnvironment) {
        final List<String> list = new ArrayList<>();
        list.add(eclipseFolder.resolve("eclipse.exe").toString());
        list.add("-data");
        list.add(projectEnvironment.resolve("workspace").toString());
        list.addAll(getEclipseReleaseBean().getArgs());
        list.add("-vmargs");
        list.add("-Dosgi.configuration.area=file:/"
                + pathToString(projectEnvironment.resolve("eclipse").resolve("configuration")));
        // should be quoted as it contains the special characters that might not work
        list.add(quoteArg(
                "-Doomph.redirection.index.redirection=index:/->" + "file:/"
                + pathToString(getConfiguration().getRelativeToConfigFile(getEclipseBean().oomphpath))
                + '/'));
        list.addAll(getEclipseReleaseBean().getVmargs());
        
        return list;
    }
    
    /**
     * <p>
     * Converts the given path to a String, replacing backslashes against slashes.
     * </p>
     * 
     * <p>
     * Windows uses backslashes, so replace them against slashes for Oomph. This has no effect on
     * Linux and Mac as there does not exist backslashes. So executing this on all platforms should
     * be OK.
     * </p>
     * 
     * @param path the path to convert
     * @return the path as {@link String}
     */
    private static final String pathToString(final Path path) {
        return path.toString().replace('\\', '/');
    }
    
    /**
     * Quotes the argument.
     * </br>
     * Should be used if the argument contain special characters.
     * 
     * @param arg arguments
     * @return quoted argument.
     */
    private static final String quoteArg(final String arg) {
        return "\"" + arg + "\"";
    }

    /**
     * Sets the bundle pool to the desired configured location.
     */
    private void setBundlePool() {
        final String bundlePoolString = getEclipseBean().getBundlePool();
        if (bundlePoolString != null) {
            try {
                final Path bundlePool = Paths.get(bundlePoolString);
                final String bundlePoolConfigString = getEclipseBean().getBundlePoolConfig();
                final Path bundlePoolConfigPath;
                if (bundlePoolConfigString == null) {
                    // Default location
                    bundlePoolConfigPath = Paths.get(System.getProperty("user.home"), ".eclipse",
                            "org.eclipse.oomph.p2");
                } else {
                    // Split slash and backslash
                    bundlePoolConfigPath = Paths.get(bundlePoolConfigString);
                }
                if (Files.notExists(bundlePoolConfigPath)) {
                    Files.createDirectories(bundlePoolConfigPath);
                }
                createAgentsInfo(bundlePool, bundlePoolConfigPath);
                createDefaultsInfo(bundlePool, bundlePoolConfigPath);
            } catch (IOException | InvalidPathException e) {
                LOGGER.error("Could not set bundle pool.", e);
            }
        }
    }

    /**
     * Creates the file "agents.info".
     * 
     * @param bundlePool the bundle pool to set
     * @param bundlePoolConfigPath the path where the file "agents.info" should exist.
     * @throws IOException
     */
    private void createAgentsInfo(final Path bundlePool, final Path bundlePoolConfigPath)
            throws IOException {
        final Path agentsInfo = bundlePoolConfigPath.resolve("agents.info");
        if (Files.notExists(agentsInfo)) {
            Files.createFile(agentsInfo);
            Files.writeString(agentsInfo, bundlePool.toString());
        } else {
            final List<String> existingLines = Files.readAllLines(agentsInfo);
            if (existingLines.isEmpty()) {
                // Empty file
                Files.writeString(agentsInfo, bundlePool.toString());
            } else if (bundlePool.equals((Paths.get(existingLines.get(0))))) {
                // Nothing to do, already set correct
            } else if (existingLines.contains(bundlePool.toString())) {
                // Entry exists but is not top, move to top
                existingLines.remove(bundlePool.toString());
                existingLines.add(0, bundlePool.toString());
                Files.write(agentsInfo, existingLines);
            } else {
                // Entry does not exist, set on top
                existingLines.add(0, bundlePool.toString());
                Files.write(agentsInfo, existingLines);
            }
        }
    }

    /**
     * Creates the file "defaults.info".
     * 
     * @param bundlePool the bundle pool to set
     * @param bundlePoolConfigPath the path where the file "defaults.info" should exist.
     * @throws IOException
     */
    private void createDefaultsInfo(final Path bundlePool, final Path bundlePoolConfigPath)
            throws IOException {
        final Path defaultsInfo = bundlePoolConfigPath.resolve("defaults.info");
        final Properties p = new Properties(2);
        if (Files.exists(defaultsInfo)) {
            try (final InputStream is = Files.newInputStream(defaultsInfo)) {
                p.load(is);
            }
        }
        p.setProperty("org.eclipse.oomph.setup.ui", bundlePool.resolve("pool").toString());
        p.setProperty("org.eclipse.oomph.setup.ui:agent", bundlePool.toString());
        try (final OutputStream os = Files.newOutputStream(defaultsInfo)) {
            p.store(os, null);
        }
    }

    /**
     * Creates "installation.setup" for the Eclipse instance and it's required directory structure.
     * 
     * @param projectEnvironment the project environment
     * @throws TransformerFactoryConfigurationError
     */
    private void createEclipseOomphSetup(final Path projectEnvironment)
            throws TransformerFactoryConfigurationError {
        final Path eclipseSetup = projectEnvironment //
                .resolve("eclipse") //
                .resolve("configuration") //
                .resolve("org.eclipse.oomph.setup") //
                .resolve("installation.setup");
        if (Files.notExists(eclipseSetup)) {
            LOGGER.trace("Creating Eclipse Oomph Setup '{}'", eclipseSetup);
            final Path eclipseWorkspace = eclipseSetup.getParent();
            try {
                Files.createDirectories(eclipseWorkspace);
                LOGGER.trace("Workspace directory '{}' created.", eclipseWorkspace);
            } catch (IOException e) {
                LOGGER.error("Could not create workspaces directory.", e);
            }
        }
        try {
            final Document document = createEclipseOomphSetupDocument();
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
            transformer.transform(new DOMSource(document), new StreamResult(eclipseSetup.toFile()));
            LOGGER.trace("Eclipse Oomph Setup file '{}' created", eclipseSetup);
        } catch (ParserConfigurationException | TransformerException e) {
            LOGGER.error("Could not create Eclipse Oomph Setup file.", e);
        }
    }

    /**
     * Creates "installation.setup" for the Eclipse instance.
     * 
     * @return the XML document representing the "installation.setup"
     * @throws ParserConfigurationException
     */
    private Document createEclipseOomphSetupDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        final Document document = factory.newDocumentBuilder().newDocument();

        final Element setup = document.createElement("setup:Installation");
        document.appendChild(setup);
        setup.setAttribute("xmi:version", "2.0");
        setup.setAttribute("xmlns:xmi", "http://www.omg.org/XMI");
        setup.setAttribute("xmlns:setup", "http://www.eclipse.org/oomph/setup/1.0");
        setup.setAttribute("name", "installation");

        final Element productVersion = document.createElement("productVersion");
        setup.appendChild(productVersion);
        productVersion.setAttribute("href", String.format(
                "index:/org.eclipse.setup#//@productCatalogs[name='%s']/@products[name='epp.package.java']/@versions[name='empty']",
                getEclipseBean().productCatalog));
        return document;
    }

    /**
     * Creates "workspace.setup" for the Eclipse workspace instance and it's required directory
     * structure.
     * 
     * @param environment the environment to setup
     * @param projectEnvironment the project environment
     */
    private void createWorkspaceOomphSetup(final Path projectEnvironment) {
        final Path workspaceSetup = projectEnvironment //
                .resolve("workspace").resolve(".metadata") //
                .resolve(".plugins") //
                .resolve("org.eclipse.oomph.setup") //
                .resolve("workspace.setup");
        if (Files.notExists(workspaceSetup)) {
            LOGGER.trace("Creating Workspace Oomph Setup '{}'", workspaceSetup);
            final Path eclipseWorkspace = workspaceSetup.getParent();
            try {
                Files.createDirectories(eclipseWorkspace);
                LOGGER.trace("Workspace directory '{}' created.", eclipseWorkspace);
            } catch (IOException e) {
                LOGGER.error("Could not create workspaces directory.", e);
            }
        }
        try {
            final Document document = createWorkspaceOomphSetupDocument();
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
            transformer.transform(new DOMSource(document),
                    new StreamResult(workspaceSetup.toFile()));
            LOGGER.trace("Workspace Oomph Setup file '{}' created", workspaceSetup);
        } catch (final TransformerException e) {
            LOGGER.error("Could not create Workspace Oomph Setup file.", e);
        }
    }

    /**
     * Creates "workspace.setup" for the Eclipse workspace instance.
     * 
     * @param environment the environment to setup
     * @return the XML document representing the "workspace.setup"
     */
    private Document createWorkspaceOomphSetupDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final Document document = factory.newDocumentBuilder().newDocument();
            final Element setup = document.createElement("setup:Workspace");
            document.appendChild(setup);
            setup.setAttribute("xmi:version", "2.0");
            setup.setAttribute("xmlns:xmi", "http://www.omg.org/XMI");
            setup.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            setup.setAttribute("xmlns:setup", "http://www.eclipse.org/oomph/setup/1.0");
            setup.setAttribute("name", "workspace");

            // Set the variable git.worktree.location to identify where to import projects
            final Element variable = document.createElement("setupTask");
            setup.appendChild(variable);
            variable.setAttribute("xsi:type", "setup:VariableTask");
            variable.setAttribute("name", "git.worktree.location");
            variable.setAttribute("value",
                    pathToString(getLaunchScope().getWorktreesBaseLocation()));
            // Set the workspace name to easily identify them when several open
            final Element preference = document.createElement("setupTask");
            setup.appendChild(preference);
            preference.setAttribute("xsi:type", "setup:PreferenceTask");
            preference.setAttribute("key", "/instance/org.eclipse.ui.ide/WORKSPACE_NAME");
            preference.setAttribute("value", getLaunchScope().getId());

            final Element stream = document.createElement("stream");
            setup.appendChild(stream);
            final String hrefValue = String.format(
                    "index:/org.eclipse.setup#//@projectCatalogs[name='%s']/@projects[name='%s']/@streams[name='master']",
                    getEclipseBean().projectCatalog, getOomphProjectName());
            stream.setAttribute("href", hrefValue);
            return document;
        } catch (ParserConfigurationException e) {
            throw new GemException("Could not create Workspace Oomph Setup file.", e);
        }
    }

    /**
     * Returns the project name which should be used from the project catalog.
     * 
     * @param environment the environment to setup
     * @return the project name of the project catalog to use
     */
    private String getOomphProjectName() {
        final Path oomphPath =
                getConfiguration().getRelativeToConfigFile(getEclipseBean().oomphpath);
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final SAXParser parser = factory.newSAXParser();
            final List<String> foundProjects = new ArrayList<>(2); // Most time not more than 2
            parser.parse(oomphPath
                    .resolve(String.format("%s.setup", getEclipseBean().projectCatalog)).toFile(),
                    new DefaultHandler() {

                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void startElement(String uri, String localName, String qName,
                                Attributes attributes) throws SAXException {
                            if ("project".equals(qName)) {
                                final String href =
                                        attributes.getValue("href").replace(".setup#/", ""); // e.g.
                                                                                             // rap_190
                                final String projectId = getLaunchScope().getProject().getId(); // e.g.
                                                                                                // rap
                                final String launchScopeId = getLaunchScope().getId(); // e.g.
                                                                                       // rap_19005_HOT

                                if (href.startsWith(projectId)) {
                                    // Correct project
                                    final String href2WithoutProjectId =
                                            href.substring(projectId.length());
                                    final String launchScopeIdWithoutProjectId =
                                            launchScopeId.substring(projectId.length());
                                    if (launchScopeIdWithoutProjectId
                                            .contains(href2WithoutProjectId)) {
                                        foundProjects.add(href);
                                    }
                                }
                            }
                        }

                    });
            if (foundProjects.isEmpty()) {
                // TODO: #43 - diable/misconfigure instead of failing on launch
                throw new GemException("Oomph project for setup not found.");
            } else {
                foundProjects.sort((p1, p2) -> Integer.compare(p2.length(), p1.length()));
                return foundProjects.get(0);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new GemException(
                    String.format("Error occurred parsing Oomph files '%s'", oomphPath), e);
        }
    }
}
