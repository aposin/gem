package org.aposin.gem.core.api.config.prefs.values;

/**
 * Preference which is a switch (true/false).
 */
public abstract class BooleanPref extends IPrefValue<Boolean> {

    private Boolean value;

    public BooleanPref(final String id) {
        super(id);
    }

    public BooleanPref(final String id, final Boolean initialValue) {
        super(id);
        this.value = initialValue;
    }

    @Override
    public final Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(final Boolean value) {
        this.value = value;
    }
}
