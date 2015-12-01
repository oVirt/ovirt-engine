package org.ovirt.engine.core.utils.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

/** A test case for the {@link MultiValueMapUtils} class. */
public class MultiValueMapUtilsTest {

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    /** The key used for testing */
    private Integer key;

    /** The value used for testing */
    private Integer value;

    /** The map used for testing */
    private Map<Integer, List<Integer>> map;
    private Map<Integer, Set<Integer>> mapOfSets;

    @Before
    public void setUp() {
        key = RandomUtils.instance().nextInt();
        value = RandomUtils.instance().nextInt();
        map = new HashMap<>();
        mapOfSets = new HashMap<>();
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
        map.put(key, new LinkedList<>());
        assertAddToMap(1);
    }

    /** Tests adding a value to map that contains the testing key with an non-empty list value */
    @Test
    public void testAddToMapNonEmptyListValue() {
        List<Integer> list = new LinkedList<>();
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

    @Test
    public void testRemoveMapSingleEntry() {
        MultiValueMapUtils.addToMap(key, value, map);
        assertTrue(MultiValueMapUtils.removeFromMap(map, key, value));
        assertEquals(0, map.size());
    }

    @Test
    public void testRemoveMapSeveralEntriesSameKey() {
        MultiValueMapUtils.addToMap(key, value, map);
        Integer newVal = RandomUtils.instance().nextInt();
        MultiValueMapUtils.addToMap(key, newVal, map);
        assertTrue(MultiValueMapUtils.removeFromMap(map, key, value));
        assertEquals(1, map.size());
        List<Integer> list = map.get(key);
        assertNotNull(list);
        assertEquals(1, list.size());
        Integer val = list.get(0);
        assertEquals(newVal, val);
    }

    @Test
    public void testRemoveMapKeyNotFound() {
        MultiValueMapUtils.addToMap(key, value, map);
        assertFalse(MultiValueMapUtils.removeFromMap(map, RandomUtils.instance().nextInt(), value));
        assertEquals(1, map.size());
        List<Integer> list = map.get(key);
        assertNotNull(list);
        Integer val = list.get(0);
        assertEquals(value, val);
    }

    @Test
    public void testMultiValueMapOfSets() {
        MultiValueMapUtils.addToMapOfSets(key, value, mapOfSets);
        assertEquals(mapOfSets.get(key).size(), 1);
        MultiValueMapUtils.addToMapOfSets(key, value, mapOfSets);
        assertEquals(mapOfSets.get(key).size(), 1);
        MultiValueMapUtils.addToMapOfSets(key, RandomUtils.instance().nextInt() + value, mapOfSets);
        assertEquals(mapOfSets.get(key).size(), 2);
    }


}
