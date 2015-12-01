package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VmBlockJobType implements Identifiable {
    UNKNOWN(0),
    PULL(1),
    COPY(2),
    COMMIT(3);

    private final int blockJobType;
    private static final HashMap<Integer, VmBlockJobType> mappings = new HashMap<>();

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
