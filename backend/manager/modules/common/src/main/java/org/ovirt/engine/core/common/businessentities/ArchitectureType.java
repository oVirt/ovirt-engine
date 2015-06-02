package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum ArchitectureType implements Identifiable {

    undefined(0),
    x86_64(1),
    ppc64(2),
    ppc(3),
    x86(4),
    ppc64le(5),
    ppcle(6);

    private int value;
    private static final HashMap<Integer, ArchitectureType> valueToArchitecture =
            new HashMap<Integer, ArchitectureType>();

    static {
        for (ArchitectureType architecture : values()) {
            valueToArchitecture.put(architecture.getValue(), architecture);
        }
    }

    private ArchitectureType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static ArchitectureType forValue(int value) {
        return valueToArchitecture.get(value);
    }
}
