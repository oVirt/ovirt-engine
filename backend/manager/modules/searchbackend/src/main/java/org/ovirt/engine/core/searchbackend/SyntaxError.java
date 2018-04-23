package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;
import java.util.Map;

public enum SyntaxError {
    NO_ERROR(0),
    INVALID_SEARCH_OBJECT(1),
    COLON_BEFORE_SEARCH_OBJECT(2),
    COLON_NOT_NEXT_TO_SEARCH_OBJECT(3),
    INVALID_CONDITION_FILED_OR_SORTBY(4),
    INVALID_CONDITION_RELATION(5),
    INVALID_CONDITION_VALUE(6),
    INVALID_SORT_FIELD(7),
    INVALID_SORT_DIRECTION(8),
    NOTHING_COMES_AFTER_PAGE_VALUE(9),
    CONDITION_CANT_CREATE_RRELATIONS_AC(10),
    DOT_NOT_NEXT_TO_CROSS_REF_OBJ(11),
    INVALID_POST_COLON_PHRASE(12),
    INVALID_POST_CONDITION_VALUE_PHRASE(13),
    CANT_GET_CONDITION_FIELD_AC(14),
    CANT_GET_CONDITION_RELATIONS_AC(15),
    INVALID_CONDITION_FILED(16),
    UNIDENTIFIED_STATE(17),
    INVALID_POST_OR_AND_PHRASE(18),
    INVALID_POST_CROSS_REF_OBJ(19),
    FREE_TEXT_ALLOWED_ONCE_PER_OBJ(20),
    INVALID_CHARECTER(21),
    INVALID_PAGE_FEILD(22);

    private int intValue;
    private static Map<Integer, SyntaxError> mappings;

    private static synchronized Map<Integer, SyntaxError> getMappings() {
        if (mappings == null) {
            mappings = new HashMap<>();
        }
        return mappings;
    }

    private SyntaxError(int value) {
        intValue = value;
        SyntaxError.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static SyntaxError forValue(int value) {
        return getMappings().get(value);
    }
}
