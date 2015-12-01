package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum VmPauseStatus {
    NONE(0, false),
    EOTHER(1, true),
    EIO(2, true),
    ENOSPC(3, true),
    EPERM(4, true),
    NOERR(5, false);

    private static final Map<Integer, VmPauseStatus> mappings = new HashMap<>();
    private final boolean isError;
    private int value;

    static {
        mappings.put(0, NONE);
        mappings.put(1, EOTHER);
        mappings.put(2, EIO);
        mappings.put(3, ENOSPC);
        mappings.put(4, EPERM);
        mappings.put(5, NOERR);
    }

    VmPauseStatus(int value, boolean isError) {
        this.value = value;
        this.isError = isError;
    }

    public int getValue() {
        return value;
    }

    public boolean isError() {
        return isError;
    }

    public static VmPauseStatus forValue(int value) {
        return mappings.get(value);
    }

}
