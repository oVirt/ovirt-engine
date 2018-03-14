package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VmPauseStatus {
    NONE(0, false),
    EOTHER(1, true),
    EIO(2, true),
    ENOSPC(3, true),
    EPERM(4, true),
    NOERR(5, false),
    POSTCOPY(6, false);

    private static final Map<Integer, VmPauseStatus> mappings =
            Stream.of(values()).collect(Collectors.toMap(VmPauseStatus::getValue, Function.identity()));
    private final boolean error;
    private int value;

    VmPauseStatus(int value, boolean error) {
        this.value = value;
        this.error = error;
    }

    public int getValue() {
        return value;
    }

    public boolean isError() {
        return error;
    }

    public static VmPauseStatus forValue(int value) {
        return mappings.get(value);
    }

}
