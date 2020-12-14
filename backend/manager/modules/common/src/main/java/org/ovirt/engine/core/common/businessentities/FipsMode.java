package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FipsMode implements Identifiable {
    UNDEFINED(0),
    DISABLED(1),
    ENABLED(2);

    private int intValue;
    private static Map<Integer, FipsMode> mappings =
            Stream.of(values()).collect(Collectors.toMap(FipsMode::getValue, Function.identity()));

    private FipsMode(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static FipsMode forValue(int value) {
        return mappings.get(value);
    }
}
