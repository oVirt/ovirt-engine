package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum ArchitectureType implements Identifiable {

    undefined(0),
    /* Host & Guest architecture */
    x86_64(1),
    /* Host & Guest architecture */
    ppc64(2),
    /* Guest architecture */
    ppc(3),
    /* Guest architecture */
    x86(4),
    /* Guest architecture */
    ppc64le(5),
    /* Guest architecture */
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
