package org.ovirt.engine.core.common.businessentities.qos;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum QosType implements Identifiable {
    ALL(0),
    STORAGE(1),
    CPU(2),
    NETWORK(3),
    HOSTNETWORK(4);

    private int value;
    private static final Map<Integer, QosType> valueToStatus = new HashMap<>();

    static {
        for (QosType status : values()) {
            valueToStatus.put(status.getValue(), status);
        }
    }

    private QosType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static QosType forValue(int value) {
        return valueToStatus.get(value);
    }
}
