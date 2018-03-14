package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Policy for assigning serial numbers to VMs
 */
public enum SerialNumberPolicy {
    /** UUID of host on which the VM ends up running will be used as the serial number. Default for backwards compatibility. */
    HOST_ID(0),
    /** UUID of the VM itself will be used. */
    VM_ID(1),
    /** Custom serial number provided in the {@code customSerialNumber} field of the respective entity will be used. */
    CUSTOM(2);

    private int value;

    private static Map<Integer, SerialNumberPolicy> mappings =
            Stream.of(values()).collect(Collectors.toMap(SerialNumberPolicy::getValue, Function.identity()));

    SerialNumberPolicy(int value) {
        this.value = value;
    }

    /**
     * Maps Integer value to policy.
     * @return If {@code value} is null returns null specifying unset policy.
     */
    public static SerialNumberPolicy forValue(Integer value) {
        return mappings.get(value);
    }

    public int getValue() {
        return value;
    }
}
