package org.ovirt.engine.core.common.businessentities.profiles;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum ProfileType implements Identifiable {
    DISK(1),
    CPU(2);

    private final int value;
    private static final Map<Integer, ProfileType> valueToStatus = new HashMap<>();

    static {
        for (ProfileType status : values()) {
            valueToStatus.put(status.getValue(), status);
        }
    }

    private ProfileType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static ProfileType forValue(int value) {
        return valueToStatus.get(value);
    }
}
