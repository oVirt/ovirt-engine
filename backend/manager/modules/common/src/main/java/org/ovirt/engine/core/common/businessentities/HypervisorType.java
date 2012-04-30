package org.ovirt.engine.core.common.businessentities;

public enum HypervisorType {
    KVM,
    Xen;

    public int getValue() {
        return this.ordinal();
    }

    public static HypervisorType forValue(int value) {
        return values()[value];
    }
}
