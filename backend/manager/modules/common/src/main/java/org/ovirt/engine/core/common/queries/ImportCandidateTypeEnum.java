package org.ovirt.engine.core.common.queries;

public enum ImportCandidateTypeEnum {
    VM,
    TEMPLATE;

    public int getValue() {
        return this.ordinal();
    }

    public static ImportCandidateTypeEnum forValue(int value) {
        return values()[value];
    }
}
