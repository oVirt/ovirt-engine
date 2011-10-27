package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum DateEnumForSearch {
    Today(1),
    Yesterday(2);

    private int intValue;
    private static java.util.HashMap<Integer, DateEnumForSearch> mappings = new HashMap<Integer, DateEnumForSearch>();

    static {
        for (DateEnumForSearch dateEnumForSearch : values()) {
            mappings.put(dateEnumForSearch.getValue(), dateEnumForSearch);
        }
    }

    private DateEnumForSearch(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static DateEnumForSearch forValue(int value) {
        return mappings.get(value);
    }
}
