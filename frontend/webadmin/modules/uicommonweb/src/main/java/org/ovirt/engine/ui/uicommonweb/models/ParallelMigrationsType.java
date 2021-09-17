package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ParallelMigrationsType {
    AUTO(-2),
    AUTO_PARALLEL(-1),
    DISABLED(0),
    CUSTOM(1);

    private int value;

    private static Map<Integer, ParallelMigrationsType> mappings =
            Stream.of(values()).collect(Collectors.toMap(ParallelMigrationsType::getValue, Function.identity()));

    ParallelMigrationsType(int value) {
        this.value = value;
    }

    /**
     * Maps Integer value to policy.
     * @return If {@code value} is null returns null specifying unset policy (cluster default for VMs).
     */
    public static ParallelMigrationsType forValue(Integer value) {
        if (value != null && value > 0) {
            return CUSTOM;
        } else {
            return mappings.get(value);
        }
    }

    public int getValue() {
        return value;
    }
}
