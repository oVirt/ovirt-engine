package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum DateEnumForSearch implements Identifiable {
    Today(1),
    Yesterday(2);

    private int intValue;
    private static final Map<Integer, DateEnumForSearch> mappings = new HashMap<>();

    static {
        for (DateEnumForSearch dateEnumForSearch : values()) {
            mappings.put(dateEnumForSearch.getValue(), dateEnumForSearch);
        }
    }

    private DateEnumForSearch(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }


    public static DateEnumForSearch forValue(int value) {
        return mappings.get(value);
    }
}
