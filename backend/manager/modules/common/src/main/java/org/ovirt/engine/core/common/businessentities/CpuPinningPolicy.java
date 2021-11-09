package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Policy for assigning automatic CPU and NUMA pinning
 */
public enum CpuPinningPolicy {
    NONE(0),
    /** Use the user provided manual string for pinning **/
    MANUAL(1),
    /** Switch the VM topology to be maximized on the selected host. Sets the CPU pinning and NUMA accordingly */
    RESIZE_AND_PIN_NUMA(2);

    private int value;

    private static Map<Integer, CpuPinningPolicy> mappings =
            Stream.of(values()).collect(Collectors.toMap(CpuPinningPolicy::getValue, Function.identity()));

    CpuPinningPolicy(int value) {
        this.value = value;
    }

    /**
     * Maps Integer value to policy.
     * @return If {@code value} is null returns null specifying unset policy.
     */
    public static CpuPinningPolicy forValue(Integer value) {
        return mappings.get(value);
    }

    public int getValue() {
        return value;
    }
}
