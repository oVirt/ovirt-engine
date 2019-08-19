package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum VmBlockJobType implements Identifiable {
    UNKNOWN(0),
    COMMIT(3);

    private final int blockJobType;
    private static final Map<Integer, VmBlockJobType> mappings = new HashMap<>();

    static {
        for (VmBlockJobType component : values()) {
            mappings.put(component.getValue(), component);
        }
    }

    public static VmBlockJobType getByName(String name) {
        if (name == null || name.length() == 0) {
            return null;
        } else {
            for (VmBlockJobType vmBlockJobType : VmBlockJobType.values()) {
                if (vmBlockJobType.name().equalsIgnoreCase(name)) {
                    return vmBlockJobType;
                }
            }
        }
        return null;
    }

    public static VmBlockJobType forValue(int value) {
        return mappings.get(value);
    }

    private VmBlockJobType(int blockJobType) {
        this.blockJobType = blockJobType;
    }

    @Override
    public int getValue() {
        return blockJobType;
    }
}
