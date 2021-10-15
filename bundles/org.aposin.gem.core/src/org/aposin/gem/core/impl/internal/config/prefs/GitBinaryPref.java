package org.aposin.gem.core.impl.internal.config.prefs;

import org.aposin.gem.core.api.INamedObject;
import org.aposin.gem.core.api.config.prefs.PrefConstants;
import org.aposin.gem.core.api.config.prefs.values.PathPref;

/**
 * Preference for the git binary.
 */
class GitBinaryPref extends PathPref {

    // TODO: set default value if any!
    public GitBinaryPref() {
        super(PrefConstants.GIT_ID);
    }

    @Override
    public String getDisplayName() {
        return "git binary";
    }

    @Override
    public INamedObject getGroup() {
        return PrefConstants.SYTEM_GROUP;
    }

}
