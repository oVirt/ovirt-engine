package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum ArchitectureType implements Identifiable {
    // Base architectures
    undefined(0),
    /* Guest architecture */
    x86(4),
    /* Guest architecture */
    ppc(3),

    // Specific architectures
    /* Host & Guest architecture */
    x86_64(1, x86),
    /* Host & Guest architecture */
    ppc64(2, ppc),
    /* Guest architecture */
    ppc64le(5, ppc),
    /* Guest architecture */
    ppcle(6, ppc);

    private int value;
    private int family;
    private static final HashMap<Integer, ArchitectureType> valueToArchitecture = new HashMap<>();

    static {
        for (ArchitectureType architecture : values()) {
            valueToArchitecture.put(architecture.getValue(), architecture);
        }
    }

    private ArchitectureType(int value) {
        this.value = value;
        this.family = value;
    }

    private ArchitectureType(int value, ArchitectureType family) {
        this.value = value;
        this.family = family.getValue();
    }

    @Override
    public int getValue() {
        return value;
    }

    public ArchitectureType getFamily() {
        return forValue(family);
    }

    public static ArchitectureType forValue(int value) {
        return valueToArchitecture.get(value);
    }
}
