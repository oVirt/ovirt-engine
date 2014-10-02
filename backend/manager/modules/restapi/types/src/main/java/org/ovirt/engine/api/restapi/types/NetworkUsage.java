package org.ovirt.engine.api.restapi.types;

public enum NetworkUsage {
    DISPLAY,
    VM,
    MIGRATION,
    MANAGEMENT;

    public String value() {
        return name().toLowerCase();
    }

    public static NetworkUsage fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
