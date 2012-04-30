package org.ovirt.engine.core.common.queries;

public enum ImportCandidateSourceEnum {
    KVM,
    VMWARE;

    public int getValue() {
        return this.ordinal();
    }

    public static ImportCandidateSourceEnum forValue(int value) {
        return values()[value];
    }
}
