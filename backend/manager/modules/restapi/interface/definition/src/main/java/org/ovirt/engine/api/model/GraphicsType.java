package org.ovirt.engine.api.model;

public enum GraphicsType {
    SPICE, VNC;

    public String value() {
        return name().toLowerCase();
    }

    public static GraphicsType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
