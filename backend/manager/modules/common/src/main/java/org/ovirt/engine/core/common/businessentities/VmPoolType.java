package org.ovirt.engine.core.common.businessentities;

public enum VmPoolType implements Identifiable {

    AUTOMATIC(0),
    MANUAL(1);

    private int value;

    VmPoolType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static VmPoolType forValue(int value) {
        for (VmPoolType vmPoolType : values()) {
            if (vmPoolType.getValue() == value) {
                return vmPoolType;
            }
        }
        return null;
    }

}
