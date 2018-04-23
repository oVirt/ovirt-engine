package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;
import java.util.Map;

public enum SyntaxObjectType {
    BEGIN(0),
    SEARCH_OBJECT(1),
    COLON(2),
    CROSS_REF_OBJ(3),
    DOT(4),
    CONDITION_FIELD(5),
    CONDITION_RELATION(6),
    CONDITION_VALUE(7),
    OR(8),
    AND(9),
    SORTBY(10),
    SORT_FIELD(11),
    SORT_DIRECTION(12),
    PAGE(13),
    PAGE_VALUE(14),
    END(15);

    private int intValue;
    private static Map<Integer, SyntaxObjectType> mappings;

    private static synchronized Map<Integer, SyntaxObjectType> getMappings() {
        if (mappings == null) {
            mappings = new HashMap<>();
        }
        return mappings;
    }

    private SyntaxObjectType(int value) {
        intValue = value;
        SyntaxObjectType.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static SyntaxObjectType forValue(int value) {
        return getMappings().get(value);
    }
}
