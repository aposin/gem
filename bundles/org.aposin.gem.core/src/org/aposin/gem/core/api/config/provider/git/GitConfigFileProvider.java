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
package org.aposin.gem.core.api.config.provider.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import org.aposin.gem.core.GemException;
import org.aposin.gem.core.api.config.GemConfigurationException;
import org.aposin.gem.core.api.config.prefs.IPreferences;
import org.aposin.gem.core.api.config.provider.IConfigFileProvider;
import org.aposin.gem.core.impl.internal.util.GitConstants;
import org.aposin.gem.core.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidResultException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * Config file provider to get the configuration form a git repository.
 */
public class GitConfigFileProvider implements IConfigFileProvider {

    /**
     * Property to set URL for the git repository hosting the files.
     */
    public static final String GITCONFIG_URL_PROPERTY = "org.aposin.gem.gitconfig.url";

    /**
     * Property to set the branch for the git repository hosting the files.
     */
    public static final String GITCONFIG_BRANCH_PROPERTY = "org.aposin.gem.gitconfig.branch";

    /**
     * Property to set the file on the git repository with the configuration.
     */
    public static final String GITCONFIG_FILE_PROPERTY = "org.aposin.gem.gitconfig.file";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitConfigFileProvider.class);

    private final Path preferenceFile;
    private final Path gitRepoLocation;
    private final GitConfigProviderHook configHook;

    // coming from properties
    private final Path configLocation;
    private final String gitBranch;
    private final String gitUrl;

    /**
     * Default constructor
     * 
     * @param preferenceFile the preference file to use with the provider.
     * @param gitRepoLocation the folder where the git repository folder should be located.
     * @param configHook custom config hook.
     */
    public GitConfigFileProvider(final Path preferenceFile, final Path gitRepoLocation, final GitConfigProviderHook configHook) {
        this.preferenceFile = preferenceFile;
        this.gitRepoLocation = gitRepoLocation;
        this.configHook = configHook;
        // config properties
        this.gitBranch = getNotNullConfigProperty(GITCONFIG_BRANCH_PROPERTY);
        this.gitUrl = System.getProperty(GITCONFIG_URL_PROPERTY);
        this.configLocation = gitRepoLocation.resolve(getNotNullConfigProperty(GITCONFIG_FILE_PROPERTY));
    }

    public GitConfigFileProvider(final Path preferenceFile, final Path gitRepoLocation) {
        this(preferenceFile, gitRepoLocation, new GitConfigProviderHook());
    }
    
    private String getNotNullConfigProperty(final String property) {
    	final String value = System.getProperty(property);
    	if (value == null) {
            throw new GemConfigurationException(property + " property not set: required for config-repository", true);
    	}
    	return value;
    }
    
    @Override
    public Path getPrefFile() {
        if (!Files.exists(preferenceFile)) {
            try {
                ConfigUtils.createEmptyConfig(preferenceFile);
            } catch (final IOException e) {
                LOGGER.error("Unable to create prefs file", e);
            }
        }
        return preferenceFile;
    }

    /**
     * {@inheritDoc}
     * </br>
     * For the git-configuration, the following steps are performed:
     * <ul>
     * 	<li>Clone if not cloned yet (failure if origin is not properly set).</li>
     *  <li>If the configuration branch is not the correct one, check it out (if not in DEVMODE).</li>
     *  <li>Pull the latest changes (if not in DEVMODE)</li>
     * </ul>
     */
    @Override
    public Path getConfigFile(final IPreferences prefs) {
        // if it is based on the same path as the git-repository configured
        if (isConfigCloned(prefs)) {
        	checkoutConfigBranchIfRequired(prefs);
        	pullConfigRepo(prefs);
        } else {
        	cloneConfigRepo(prefs);
        }

        if (!Files.exists(configLocation)) {
            throw new GemConfigurationException("Configuration file not found: " + configLocation, true);
        }
        
        return configLocation;
    }

    @Override
    public Path getRelativeToConfigFile(final String relativePath) throws GemException {
        if (!Files.exists(configLocation)) {
            throw new GemException("Configuration not initialized or existing: " + configLocation);
        }
        
        return configLocation.getParent().resolve(relativePath);
    }

    private boolean isConfigCloned(final IPreferences prefs) {
    	if (Files.exists(gitRepoLocation.resolve(GitConstants.GITDIR_FOLDER))) {
    		try {
    			// check first the remote
		    	final String configRemote = executeCommand(gitRepoLocation, // on the repo
		    			prefs.getGitBinary().toString(), "config", "--get", "remote.origin.url");
		         if (!gitUrl.equals(configRemote)) {
                     throw new GemConfigurationException(
                             "Config-repository not on " + gitUrl + " remote (instead on " + configRemote + ")", true);
		            }
    		} catch (final IOException e) {
                throw new GemConfigurationException("Cannot check git-configuration remote!", e, true);
	    	}
    		
    		return true;
    	}
    	return false;
    }
    
    private void checkoutConfigBranchIfRequired(final IPreferences prefs) {
    	try {
    	    final String currentBranch = getCurrentConfigBranch(prefs);
    	    if (!gitBranch.equals(currentBranch)) {
    	        LOGGER.warn("Config-branch ({}) is not checked out", gitBranch);
        		if (!configHook.checkoutWhenDifferentBranch(gitBranch, currentBranch)) {
        			LOGGER.warn("CONFIGURATION NOT UP-TO-DATE: using {} branch instead of {}",
        			        currentBranch, gitBranch);
        			return;
        		}
    		    executeCommand(gitRepoLocation, // on the repo
    		    		prefs.getGitBinary().toString(), "checkout", gitBranch);
    		    LOGGER.warn("Config-branch {} was checked out", gitBranch);
    	    }
    	} catch (final IOException e) {
            throw new GemConfigurationException("Checkout of git-configuration failed!", e, true);
    	}
    }
    
    private void pullConfigRepo(final IPreferences prefs) {
    	try {
	    	executeCommand(gitRepoLocation, // on the repo
	    			prefs.getGitBinary().toString(), "pull", "origin", gitBranch);
    	} catch (final IOException e) {
    	    if (!configHook.proceedIfPullFails(gitBranch)) {
        	    throw new GemConfigurationException(MessageFormat.format(//
                        "Pulling {0} branch for git-configuration failed", gitBranch), //
                        e, true);
    	    } else {
    	        LOGGER.warn("Error pulling branch ignored: reverting pull", e);
    	        try {
    	            executeCommand(gitRepoLocation, prefs.getGitBinary().toString(), "merge", "--abort");
    	        } catch (final IOException e2) {
    	            LOGGER.warn("Error reverting pull", e2);
    	        }
    	        
    	    }
    	}
    }
    
    private void cloneConfigRepo(final IPreferences prefs) {
    	try {
	        executeCommand(null, // doesn't matter the folder
	        		prefs.getGitBinary().toString(), "clone","-b", gitBranch, //
	                gitUrl, //
	                gitRepoLocation.toAbsolutePath().toString());
    	} catch (final IOException e) {
            throw new GemConfigurationException("Clone of git-configuration failed!", e, true);
    	}
    }
    
    private String getCurrentConfigBranch(final IPreferences prefs) {
    	try {
	    	// then check if the current branch is the same
	    	return executeCommand(gitRepoLocation, //
	    			prefs.getGitBinary().toString(), "branch", "--show-current");
    	} catch (final IOException e) {
            throw new GemConfigurationException("Cannot check git-configuration branch!", e, true);
    	}
    }
    
    private String executeCommand(final Path dir, final String... args) throws IOException {
        final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        try {
            final ProcessExecutor executor = new ProcessExecutor()//
                    .directory(dir == null ? null : dir.toFile()) //
                    .exitValue(0) // only allow successful runs
                    .redirectOutput(Slf4jStream.of(LOGGER).asDebug()) //
                    .redirectError(Slf4jStream.of(LOGGER).asError()) //
                    .redirectErrorAlsoTo(errorStream) //
                    .readOutput(true) // always read output in case of error
                    .command(args);
            final ProcessResult result = executor.execute();
            return result.getOutput().getString().trim();
    	} catch (final IOException e) {
    	    throw e;
    	} catch (final InvalidResultException e) {
    	    throw new IOException(new GemException(errorStream.toString(), e));
    	} catch (final Exception e) {
    	    if (e.getCause() instanceof IOException) {
    	        throw (IOException) e.getCause();
    	    } else {
    	        throw new IOException(e);
    	    }
    	}
    }

}
