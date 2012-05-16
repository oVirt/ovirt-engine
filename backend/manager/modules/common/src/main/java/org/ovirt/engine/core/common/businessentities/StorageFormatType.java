package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum StorageFormatType {

    V1("0"),
    V2("2"),
    V3("3");

    private String intValue;
    private static java.util.HashMap<String, StorageFormatType> mappings = new HashMap<String, StorageFormatType>();

    static {
        for (StorageFormatType storageDomainFormat : values()) {
            mappings.put(storageDomainFormat.getValue(), storageDomainFormat);
        }
    }

    private StorageFormatType(String value) {
        intValue = value;
    }

    public String getValue() {
        return intValue;
    }

    public static StorageFormatType forValue(String value) {
        return mappings.get(value);
    }

}
