package org.ovirt.engine.core.common.businessentities.qos;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum QosType implements Identifiable {
    ALL(0),
    STORAGE(1);

    private int value;
    private static final Map<Integer, QosType> valueToStatus = new HashMap<Integer, QosType>();

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
