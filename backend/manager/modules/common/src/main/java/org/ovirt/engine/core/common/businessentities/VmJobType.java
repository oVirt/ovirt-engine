package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum VmJobType implements Identifiable {
    UNKNOWN(0),
    BLOCK(1);

    private final int jobType;
    private static final Map<Integer, VmJobType> mappings = new HashMap<>();

    static {
        for (VmJobType component : values()) {
            mappings.put(component.getValue(), component);
        }
    }

    public static VmJobType getByName(String name) {
        if (name == null || name.length() == 0) {
            return null;
        } else {
            for (VmJobType vmJobType : VmJobType.values()) {
                if (vmJobType.name().equalsIgnoreCase(name)) {
                    return vmJobType;
                }
            }
        }
        return null;
    }

    public static VmJobType forValue(int value) {
        return mappings.get(value);
    }

    private VmJobType(int jobType) {
        this.jobType = jobType;
    }

    @Override
    public int getValue() {
        return jobType;
    }
}
