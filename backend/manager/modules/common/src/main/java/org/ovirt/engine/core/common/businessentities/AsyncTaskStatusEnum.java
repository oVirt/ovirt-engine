package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum AsyncTaskStatusEnum {
    // unknown: task doesn't exist.
    unknown(0),
    // init: task hasn't started yet.
    init(1),
    // working: task is running.
    running(2),
    // finished: task has ended successfully.
    finished(3),
    // aborting: task has ended with failure.
    aborting(4),
    // cleaning: clean-up is being done due to 'stopTask' request or failed
    // task.
    cleaning(5);

    private int intValue;
    private static Map<Integer, AsyncTaskStatusEnum> mappings;

    static {
        mappings = new HashMap<>();
        for (AsyncTaskStatusEnum error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private AsyncTaskStatusEnum(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static AsyncTaskStatusEnum forValue(int value) {
        return mappings.get(value);
    }
}
