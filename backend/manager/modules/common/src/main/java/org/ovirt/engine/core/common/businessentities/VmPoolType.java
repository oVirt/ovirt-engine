package org.ovirt.engine.core.common.businessentities;

public enum VmPoolType implements Identifiable {
    // FIXME add ids and remove the ordinal impl of getValue
    Automatic,
    Manual;

    @Override
    public int getValue() {
        return this.ordinal();
    }

    public static VmPoolType forValue(int value) {
        return values()[value];
    }
}
