package org.ovirt.engine.api.model;

public enum PayloadEncoding {
    BASE64,
    PLAINTEXT;

    public String value() {
        return name().toLowerCase();
    }

    public static PayloadEncoding fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
