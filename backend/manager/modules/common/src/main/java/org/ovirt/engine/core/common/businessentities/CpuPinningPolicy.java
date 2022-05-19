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
    RESIZE_AND_PIN_NUMA(2),
    /** Dynamically dedicate pCPUs to be exclusively pinned to VM virtual CPU topology **/
    DEDICATED(3),
    /** Dynamically dedicate pCPUs to be exclusively pinned to VM virtual CPU topology. Each virtual CPU gets a physical core **/
    ISOLATE_THREADS(4);

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

    public boolean isExclusive() {
        return this == DEDICATED || this == ISOLATE_THREADS;
    }

    public static int compare(CpuPinningPolicy policy, CpuPinningPolicy other) {
        if (policy == other) {
            return 0;
        }
        if (policy == CpuPinningPolicy.NONE) {
            return -1;
        }
        if (other == CpuPinningPolicy.NONE) {
            return 1;
        }
        // both not none
        if (policy == CpuPinningPolicy.MANUAL) {
            return 1;
        }
        if (other == CpuPinningPolicy.MANUAL) {
            return -1;
        }
        // not none, not manual
        if (policy == CpuPinningPolicy.RESIZE_AND_PIN_NUMA) {
            // automatically vm2 is exclusive
            return 1;
        }
        if (other == CpuPinningPolicy.RESIZE_AND_PIN_NUMA) {
            // automatically vm1 is exclusive
            return -1;
        }
        if (policy == CpuPinningPolicy.ISOLATE_THREADS) {
            // automatically vm2 is dedicated
            return 1;
        }
        // vm1 is dedicated and vm2 is isolate-threads
        return -1;
    }
}
