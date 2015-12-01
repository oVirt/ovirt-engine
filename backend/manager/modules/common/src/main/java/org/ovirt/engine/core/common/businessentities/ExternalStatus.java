package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum ExternalStatus implements Identifiable {
    Ok(0),
    Info(1),
    Warning(2),
    Error(3),
    Failure(4);

    private static final Map<Integer, ExternalStatus> mappings = new HashMap<>();
    private int id;

    static {
        for (ExternalStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private ExternalStatus(int value) {
        id = value;
    }

    @Override
    public int getValue() {
        return id;
    }

    public static ExternalStatus forValue(int value) {
        return mappings.get(value);
    }
}
