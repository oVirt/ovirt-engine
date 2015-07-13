package org.ovirt.engine.ui.uicommonweb.models.tags;

@SuppressWarnings("unused")
public enum TagModelType {
    Regular,
    ReadOnly,
    Root;

    public int getValue() {
        return this.ordinal();
    }

    public static TagModelType forValue(int value) {
        return values()[value];
    }
}
