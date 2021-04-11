package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LeaseJobStatus {
    Pending("PENDING"),
    Failed("FAILED"),
    Succeeded("SUCCEEDED"),
    Fenced("FENCED");

    private String value;
    private static final Map<String, LeaseJobStatus> valueToStatus =
            Stream.of(values()).collect(Collectors.toMap(LeaseJobStatus::getValue, Function.identity()));

    LeaseJobStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LeaseJobStatus forValue(String value) {
        return valueToStatus.get(value);
    }
}
