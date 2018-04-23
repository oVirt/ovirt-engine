package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum CacheModeType {
    writethrough("0"),
    writeback("1");

    private String value;
    private static final Map<String, CacheModeType> mappings = new HashMap<>();

    static {
        for (CacheModeType cacheModeType : values()) {
            mappings.put(cacheModeType.getValue(), cacheModeType);
        }
    }

    private CacheModeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
