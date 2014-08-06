package org.ovirt.engine.api.model;

public enum QosType {
    STORAGE,
    CPU;

    public String value() {
        return name().toLowerCase();
    }

    public static QosType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
