package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageFormatType")
public enum StorageFormatType {

    V1("0"),
    V2("2");

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
