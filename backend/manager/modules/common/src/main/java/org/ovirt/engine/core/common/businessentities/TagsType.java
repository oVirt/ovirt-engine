package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum TagsType {
    GeneralTag(0),
    AdElementTag(1);

    private int intValue;
    private static final HashMap<Integer, TagsType> mappings = new HashMap<>();

    static {
        for (TagsType tagsType : values()) {
            mappings.put(tagsType.getValue(), tagsType);
        }
    }

    private TagsType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static TagsType forValue(int value) {
        return mappings.get(value);
    }
}
