package org.aposin.gem.core.api.config.prefs.values;

/**
 * Preference which is a simple string.
 */
public abstract class StringPref extends IPrefValue<String> {

    private String value;

    public StringPref(final String id) {
        super(id);
    }

    public StringPref(final String id, final String initialValue) {
        super(id);
        this.value = initialValue;
    }

    @Override
    public final String getValue() {
        return value;
    }

    @Override
    public void setValue(final String value) {
        this.value = value;
    }
}
