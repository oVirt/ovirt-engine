package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OriginType {
    RHEV(0),
    VMWARE(1),
    XEN(2),
    OVIRT(3),
    // VMs that externally run on the host (not created by the engine)
    EXTERNAL(4),
    // VMs that were created by the hosted engine setup
    HOSTED_ENGINE(5),
    // managed means we allow limited provisioning on this VM by the engine
    MANAGED_HOSTED_ENGINE(6),
    KVM(7),
    PHYSICAL_MACHINE(8),
    HYPERV(9),
    KUBEVIRT(10);

    private int intValue;
    private static Map<Integer, OriginType> mappings =
            Stream.of(values()).collect(Collectors.toMap(OriginType::getValue, Function.identity()));

    private OriginType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static OriginType forValue(int value) {
        return mappings.get(value);
    }
}
