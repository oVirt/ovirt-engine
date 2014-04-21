package org.ovirt.engine.api.model;

public enum NumaTuneMode {
    STRICT,
    INTERLEAVE,
    PREFERRED;

    public String value() {
        return name().toLowerCase();
    }

    public static NumaTuneMode fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
