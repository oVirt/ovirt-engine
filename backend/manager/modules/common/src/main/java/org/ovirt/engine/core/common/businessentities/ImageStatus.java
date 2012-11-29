package org.ovirt.engine.core.common.businessentities;

public enum ImageStatus implements Identifiable {
    // FIXME add ids and remove the ordinal impl of getValue
    Unassigned,
    OK,
    LOCKED,
    INVALID,
    ILLEGAL;

    @Override
    public int getValue() {
        return this.ordinal();
    }

    public static ImageStatus forValue(int value) {
        return values()[value];
    }

}
