package org.ovirt.engine.api.model;

public enum SsoMethod {

    GUEST_AGENT;

    public String value() {
        return name().toLowerCase();
    }

    public static SsoMethod fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
