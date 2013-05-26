package org.ovirt.engine.api.model;

public enum StepEnum {
    VALIDATING,
    EXECUTING,
    FINALIZING,
    UNKNOWN;

    public String value() {
        return name().toLowerCase();
    }

    public static StepEnum fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
