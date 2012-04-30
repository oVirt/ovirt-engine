package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

public enum RegisterableQueryReturnDataType implements Serializable {
    UNDEFINED,
    IQUERYABLE,
    LIST_IQUERYABLE,
    SEARCH;

    public int getValue() {
        return this.ordinal();
    }

    public static RegisterableQueryReturnDataType forValue(int value) {
        return values()[value];
    }
}
