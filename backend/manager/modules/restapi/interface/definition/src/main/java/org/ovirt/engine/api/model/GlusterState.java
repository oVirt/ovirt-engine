package org.ovirt.engine.api.model;

public enum GlusterState {

    UP,
    DOWN;

    public String value() {
        return name();
    }

    public static GlusterState fromValue(String v) {
        return valueOf(v);
    }

}
