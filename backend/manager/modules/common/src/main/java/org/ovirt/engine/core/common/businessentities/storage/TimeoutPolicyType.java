package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum TimeoutPolicyType implements Identifiable {
    LEGACY(0, "legacy"),
    PAUSE(1, "pause"),
    CANCEL(2, "cancel");

    private int value;
    private String description;
    private static final Map<Integer, TimeoutPolicyType> valueToType = Stream.of(values())
            .collect(Collectors.toMap(TimeoutPolicyType::getValue, Function.identity()));

    private static final Map<String, TimeoutPolicyType> descriptionToType = Stream.of(values())
            .collect(Collectors.toMap(TimeoutPolicyType::getDescription, Function.identity()));

    TimeoutPolicyType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    private String getDescription() {
        return description;
    }

    public static TimeoutPolicyType forValue(int value) {
        return valueToType.getOrDefault(value, LEGACY);
    }

    public static TimeoutPolicyType forString(String value) {
        return descriptionToType.getOrDefault(value.toLowerCase(), LEGACY);
    }

    @Override
    public String toString() {
        return description;
    }
}
