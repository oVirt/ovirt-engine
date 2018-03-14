package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum GuestAgentStatus {
    DoesntExist(0),
    Exists(1),
    UpdateNeeded(2);

    private static final Map<Integer, GuestAgentStatus> mappings =
            Stream.of(values()).collect(Collectors.toMap(GuestAgentStatus::getValue, Function.identity()));
    private int value;

    GuestAgentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GuestAgentStatus forValue(int value) {
        return mappings.get(value);
    }

}
