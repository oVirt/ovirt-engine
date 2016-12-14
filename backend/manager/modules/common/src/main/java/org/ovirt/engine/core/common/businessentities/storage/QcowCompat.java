package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;

public enum QcowCompat {
    Undefined(0, "Undefined"),
    QCOW2_V2(1, "0.10"),
    QCOW2_V3(2, "1.1");

    private int value;
    private String compatValue;
    private static final HashMap<String, QcowCompat> mappingByCompatValue = new HashMap<>();
    private static final HashMap<Integer, QcowCompat> mappingByValue = new HashMap<>();

    static {
        QcowCompat[] enumValues = values();
        for (int i = 0; i < enumValues.length; i++) {
            mappingByCompatValue.put(enumValues[i].getCompatValue(), enumValues[i]);
            mappingByValue.put(enumValues[i].getValue(), enumValues[i]);
        }
    }

    QcowCompat(int value, String compatValue) {
        this.value = value;
        this.compatValue = compatValue;
    }

    public int getValue() {
        return value;
    }

    public String getCompatValue() {
        return compatValue;
    }

    public static QcowCompat forValue(int value) {
        return mappingByValue.get(value);
    }

    public static QcowCompat forCompatValue(String compatValue) {
        return mappingByCompatValue.get(compatValue);
    }
}
