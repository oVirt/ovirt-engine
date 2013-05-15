package org.ovirt.engine.api.model;

public enum ScsiGenericIO {
    FILTERED,
    UNFILTERED;

    public String value() {
        return name().toLowerCase();
    }

    public static ScsiGenericIO fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
