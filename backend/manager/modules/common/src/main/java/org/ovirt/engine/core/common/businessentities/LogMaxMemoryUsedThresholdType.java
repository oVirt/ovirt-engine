package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LogMaxMemoryUsedThresholdType implements Identifiable {
    /**
     * Percentage of total memory
     **/
    PERCENTAGE(0),
    /**
     * Absolute value of memory in MB
     **/
    ABSOLUTE_VALUE(1);

    private int thresholdType;

    private static Map<Integer, LogMaxMemoryUsedThresholdType> mappings =
            Stream.of(values()).collect(Collectors.toMap(LogMaxMemoryUsedThresholdType::getValue, Function.identity()));

    private LogMaxMemoryUsedThresholdType(int thresholdType) {
        this.thresholdType = thresholdType;
    }

    public static LogMaxMemoryUsedThresholdType forValue(Integer value) {
        return mappings.get(value);
    }

    @Override
    public int getValue() {
        return thresholdType;
    }
}
