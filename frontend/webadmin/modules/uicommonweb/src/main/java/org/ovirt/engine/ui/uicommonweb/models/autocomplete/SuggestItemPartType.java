package org.ovirt.engine.ui.uicommonweb.models.autocomplete;

@SuppressWarnings("unused")
public enum SuggestItemPartType {
    Valid,
    New,
    Erroneous;

    public int getValue() {
        return this.ordinal();
    }

    public static SuggestItemPartType forValue(int value) {
        return values()[value];
    }
}
