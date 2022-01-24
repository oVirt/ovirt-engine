package org.ovirt.engine.core.common.migration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ParallelMigrationsType {
    AUTO(-2),
    AUTO_PARALLEL(-1),
    DISABLED(0),
    CUSTOM(1);

    // It's possible to use only one "parallel" connection, but it's safer to always
    // use at least 2 with the current QEMU implementation.
    public static int MIN_PARALLEL_CONNECTIONS = 2;
    // QEMU/libvirt accepts uint8_t values for the number of connections.
    public static int MAX_PARALLEL_CONNECTIONS = 255;

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
