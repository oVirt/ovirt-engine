package org.ovirt.engine.core.common.scheduling;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum PolicyUnitType implements Identifiable {
    Filter(0),
    Weight(1),
    LoadBalancing(2);

    private int intValue;
    private static final java.util.HashMap<Integer, PolicyUnitType> mappings = new HashMap<Integer, PolicyUnitType>();

    static {
        for (PolicyUnitType vmType : values()) {
            mappings.put(vmType.getValue(), vmType);
        }
    }

    private PolicyUnitType(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static PolicyUnitType forValue(int value) {
        return mappings.get(value);
    }
}
