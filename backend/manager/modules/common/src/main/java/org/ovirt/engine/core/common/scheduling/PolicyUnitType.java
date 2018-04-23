package org.ovirt.engine.core.common.scheduling;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum PolicyUnitType implements Identifiable {
    FILTER(0),
    WEIGHT(1),
    LOAD_BALANCING(2),
    SELECTOR(3);

    private int intValue;
    private static final Map<Integer, PolicyUnitType> mappings = new HashMap<>();

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
