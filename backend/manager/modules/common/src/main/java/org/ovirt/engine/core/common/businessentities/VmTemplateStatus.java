package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum VmTemplateStatus implements Identifiable {
    OK(0),
    Locked(1),
    Illegal(2);

    private int intValue;
    private static final java.util.HashMap<Integer, VmTemplateStatus> mappings = new HashMap<Integer, VmTemplateStatus>();

    static {
        for (VmTemplateStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private VmTemplateStatus(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static VmTemplateStatus forValue(int value) {
        return mappings.get(value);
    }
}
