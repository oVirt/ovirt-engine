package org.ovirt.engine.api.model;

/**
 * This enum holds the types of all internal policy units types
 */
public enum PolicyUnitType {
    FILTER,
    WEIGHT,
    LOAD_BALANCING;


    public String value() {
        return name().toLowerCase();
    }

    public static PolicyUnitType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
