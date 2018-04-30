package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.ovirt.engine.core.common.queries.QueryType;

/** A helper class containing utilities to help test query types */
final class TestHelperQueriesCommandType {
    /** @return The private type field, via reflection */
    private static Field getAccessibleQueryTypeField() {
        for (Field f : QueriesCommandBase.class.getDeclaredFields()) {
            if (f.getName().equals("queryType")) {
                f.setAccessible(true);
                return f;
            }
        }
        fail("Can't find the type field");
        return null;
    }

    /**
     * Sets the query type, via reflection
     * @param query The query to set the type on
     * @param type The type to set
     */
    public static void setQueryTypeFieldValue(QueriesCommandBase<?> query, QueryType type)
            throws IllegalArgumentException, IllegalAccessException {
        Field f = getAccessibleQueryTypeField();
        f.set(query, type);
    }

    /** @return The private type, via reflection */
    public static QueryType getQueryTypeFieldValue(QueriesCommandBase<?> query)
            throws IllegalArgumentException, IllegalAccessException {
        return (QueryType) getAccessibleQueryTypeField().get(query);
    }
}
