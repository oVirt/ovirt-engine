package org.ovirt.engine.api.model;

import org.apache.commons.lang.StringUtils;

/**
 * Enum representing boolean value that can be either set or inherited from higher level.
 * Usual inheritance order is VM -> Cluster -> engine-config.
 */
public enum InheritableBoolean {
    /** Set value to true on this level. */
    TRUE,
    /** Set value to false on this level. */
    FALSE,
    /** Inherit value from higher level. */
    INHERIT;

    public String value() {
        return name().toLowerCase();
    }

    public static InheritableBoolean fromValue(String value) {
        try {
            return StringUtils.isEmpty(value) ? null : valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
