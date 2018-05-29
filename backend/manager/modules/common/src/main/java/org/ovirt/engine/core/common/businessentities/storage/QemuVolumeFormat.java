package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum QemuVolumeFormat {
    RAW("raw"),
    QCOW2("qcow2");

    private String value;
    private static final Map<String, QemuVolumeFormat> mappings = new HashMap<>();

    static {
        for (QemuVolumeFormat format : values()) {
            mappings.put(format.getValue(), format);
        }
    }

    QemuVolumeFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QemuVolumeFormat forValue(String value) {
        return mappings.get(value);
    }
}
