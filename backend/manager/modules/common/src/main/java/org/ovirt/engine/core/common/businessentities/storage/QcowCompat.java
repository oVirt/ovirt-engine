package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum QcowCompat {
    Undefined(0, "Undefined", 0),
    QCOW2_V2(1, "0.10", 2),
    QCOW2_V3(2, "1.1", 3);

    private int value;
    private String compatValue;
    private int qcowHeaderVersion;
    private static final Map<String, QcowCompat> mappingByCompatValue = new HashMap<>();
    private static final Map<Integer, QcowCompat> mappingByValue = new HashMap<>();
    private static final Map<Integer, QcowCompat> mappingByQcowHeaderVersion = new HashMap<>();

    static {
        QcowCompat[] enumValues = values();
        for (int i = 0; i < enumValues.length; i++) {
            mappingByCompatValue.put(enumValues[i].getCompatValue(), enumValues[i]);
            mappingByValue.put(enumValues[i].getValue(), enumValues[i]);
            mappingByQcowHeaderVersion.put(enumValues[i].getQcowHeaderVersion(), enumValues[i]);
        }
    }

    QcowCompat(int value, String compatValue, int qcowHeaderVersion) {
        this.value = value;
        this.compatValue = compatValue;
        this.qcowHeaderVersion = qcowHeaderVersion;
    }

    public int getValue() {
        return value;
    }

    public String getCompatValue() {
        return compatValue;
    }

    public int getQcowHeaderVersion() {
        return qcowHeaderVersion;
    }

    public static QcowCompat forValue(int value) {
        return mappingByValue.get(value);
    }

    public static QcowCompat forCompatValue(String compatValue) {
        return mappingByCompatValue.get(compatValue);
    }

    public static QcowCompat forQcowHeaderVersion(int qcowHeaderVersion) {
        return mappingByQcowHeaderVersion.get(qcowHeaderVersion);
    }
}
