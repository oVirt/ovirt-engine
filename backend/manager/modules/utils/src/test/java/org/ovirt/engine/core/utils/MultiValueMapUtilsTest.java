package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/** A test case for the {@link MultiValueMapUtils} class. */
public class MultiValueMapUtilsTest {

    /** The key used for testing */
    private Integer key;

    /** The value used for testing */
    private Integer value;

    /** The map used for testing */
    private Map<Integer, List<Integer>> map;

    @Before
    public void setUp() {
        key = RandomUtils.instance().nextInt();
        value = RandomUtils.instance().nextInt();
        map = new HashMap<Integer, List<Integer>>();
    }

    /** Tests adding a value to map that does not contain the testing key */
    @Test
    public void testAddToMapNoKey() {
        assertAddToMap(1);
    }

    /** Tests adding a value to map that contains the testing key with a null value */
    @Test
    public void testAddToMapNullValue() {
        map.put(key, null);
        assertAddToMap(1);
    }

    /** Tests adding a value to map that contains the testing key with an empty list value */
    @Test
    public void testAddToMapEmptyListValue() {
        map.put(key, new LinkedList<Integer>());
        assertAddToMap(1);
    }

    /** Tests adding a value to map that contains the testing key with an non-empty list value */
    @Test
    public void testAddToMapNonEmptyListValue() {
        List<Integer> list = new LinkedList<Integer>();
        list.add(value);
        map.put(key, list);
        assertAddToMap(2);
    }

    private void assertAddToMap(int expectedNumValues) {
        MultiValueMapUtils.addToMap(key, value, map);
        assertEquals("Wrong number of keys in the map", 1, map.size());
        assertEquals("Wrong number of values in the list", expectedNumValues, map.get(key).size());
        for (int i = 0; i < expectedNumValues; ++i) {
            assertEquals("Wrong value in the list", value, map.get(key).get(i));
        }
    }
}
