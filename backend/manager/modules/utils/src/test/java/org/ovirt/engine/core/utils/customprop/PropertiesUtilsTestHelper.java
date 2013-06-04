package org.ovirt.engine.core.utils.customprop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

/**
 * Class with helper methods to ease properties testing
 */
public class PropertiesUtilsTestHelper {
    /**
     * Validates if property map is not null and its size matches specified size
     *
     * @param map
     *            property map
     * @param size
     *            specified map size
     */
    public static void validatePropertyMap(Map<?, ?> map, int size) {
        assertNotNull(map);
        assertEquals(size, map.size());
    }

    /**
     * Validates if property exists and its value matches the specified value
     *
     * @param map
     *            properties map
     * @param key
     *            property name
     * @param value
     *            property value
     */
    public static void validatePropertyValue(Map<String, String> map, String key, String value) {
        String val = map.get(key);
        assertNotNull(val);
        assertEquals(value, val);
    }
}
