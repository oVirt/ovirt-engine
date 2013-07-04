package org.ovirt.engine.api.model;

public enum ConfigurationType {
    OVF;

    public static ConfigurationType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String value() {
        return name().toLowerCase();
    }
}
