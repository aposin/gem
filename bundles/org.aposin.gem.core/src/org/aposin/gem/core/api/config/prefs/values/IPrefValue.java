package org.aposin.gem.core.api.config.prefs.values;

import org.aposin.gem.core.api.INamedObject;

/**
 * Preference value abstract class.
 * </br>
 * This should not be extended by the client directly,
 * but other sub-classes. Every new sub-class should be
 * handled by the preference framerork or code using the API.
 *
 * @param <T> value for the preference.
 */
public abstract class IPrefValue<T> implements INamedObject {

    private final String id;

    /**
     * Initialize preference value.
     * 
     * @param id preference value ID.
     */
    IPrefValue(final String id) {
        this.id = id;
    }

    /**
     * Gets the ID for the preference.
     */
    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return getId();
    }

    /**
     * Gets the group for the preference.
     * 
     * @return group for the preference.
     */
    public abstract INamedObject getGroup();

    /**
     * Gets the parameter value.
     * 
     * @return value.
     */
    public abstract T getValue();

    /**
     * Sets the parameter value.
     * 
     * @param value the value.
     */
    public abstract void setValue(final T value);

}