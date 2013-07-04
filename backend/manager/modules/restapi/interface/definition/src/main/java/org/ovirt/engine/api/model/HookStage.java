package org.ovirt.engine.api.model;

public enum HookStage {
    PRE,
    POST;

    public String value() {
        return name().toLowerCase();
    }

    public static HookStage fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
