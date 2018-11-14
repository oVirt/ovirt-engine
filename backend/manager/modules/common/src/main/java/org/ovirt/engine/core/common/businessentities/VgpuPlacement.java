package org.ovirt.engine.core.common.businessentities;

import java.util.Arrays;

public enum VgpuPlacement implements Identifiable {
    CONSOLIDATED(1),
    SEPARATED(2);

    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 2;

    private final int value;

    private VgpuPlacement(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static VgpuPlacement forValue(int value) {
        return Arrays.stream(values()).filter(e -> e.getValue() == value).findFirst().get();
    }
}
