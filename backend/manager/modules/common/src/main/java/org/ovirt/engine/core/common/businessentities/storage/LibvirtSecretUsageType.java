package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum LibvirtSecretUsageType {
    CEPH(0);

    private Integer value;
    private static final Map<Integer, LibvirtSecretUsageType> mappings = new HashMap<>();

    static {
        for (LibvirtSecretUsageType volumeType : values()) {
            mappings.put(volumeType.getValue(), volumeType);
        }
    }

    LibvirtSecretUsageType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static LibvirtSecretUsageType forValue(Integer value) {
        return mappings.get(value);
    }

}
