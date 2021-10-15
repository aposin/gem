package org.aposin.gem.core.api.config.prefs.values;

import java.nio.file.Path;

/**
 * Preference which is a path.
 */
public abstract class PathPref extends IPrefValue<Path> {

    private Path value;

    public PathPref(final String id) {
        super(id);
    }

    public PathPref(final String id, final Path initialValue) {
        super(id);
        this.value = initialValue;
    }

    @Override
    public final Path getValue() {
        return value;
    }

    @Override
    public void setValue(final Path value) {
        this.value = value;
    }
}
