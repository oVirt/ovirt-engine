package org.ovirt.engine.api.model;

public enum Architecture {
    UNDEFINED,
    X86_64,
    PPC64;

    public String value() {
        return name();
    }

    public static Architecture fromValue(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
