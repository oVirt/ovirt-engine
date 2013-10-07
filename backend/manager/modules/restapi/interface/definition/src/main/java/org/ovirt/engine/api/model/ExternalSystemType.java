package org.ovirt.engine.api.model;

public enum ExternalSystemType {
    VDSM,
    GLUSTER;

    public String value() {
        return name().toLowerCase();
    }

    public static ExternalSystemType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
