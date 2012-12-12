package org.ovirt.engine.api.model;

public enum CpuMode {

    CUSTOM,
    HOST_MODEL,
    HOST_PASSTHROUGH;

    public String value() {
        return name().toLowerCase();
    }

    public static CpuMode fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
