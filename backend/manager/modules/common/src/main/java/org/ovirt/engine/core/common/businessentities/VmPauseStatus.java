package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum VmPauseStatus {
    NONE(0),
    EOTHER(1),
    EIO(2),
    ENOSPC(3),
    EPERM(4),
    NOERR(5);

    private static final Map<Integer, VmPauseStatus> mappings = new HashMap<Integer, VmPauseStatus>();
    private int value;

    static {
        mappings.put(0, NONE);
        mappings.put(1, EOTHER);
        mappings.put(2, EIO);
        mappings.put(3, ENOSPC);
        mappings.put(4, EPERM);
        mappings.put(5, NOERR);
    }

    VmPauseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static VmPauseStatus forValue(int value) {
        return mappings.get(value);
    }

}
