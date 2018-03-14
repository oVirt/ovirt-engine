package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VmType implements Identifiable {
    Desktop(0),
    Server(1),
    HighPerformance(2);

    private int intValue;
    private static final Map<Integer, VmType> mappings =
            Stream.of(values()).collect(Collectors.toMap(VmType::getValue, Function.identity()));

    private VmType(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static VmType forValue(int value) {
        return mappings.get(value);
    }
}
