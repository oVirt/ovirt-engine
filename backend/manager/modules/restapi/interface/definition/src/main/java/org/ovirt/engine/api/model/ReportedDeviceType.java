package org.ovirt.engine.api.model;

public enum ReportedDeviceType {
    NETWORK;

    public String value() {
        return name().toLowerCase();
    }

    public static ReportedDeviceType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
