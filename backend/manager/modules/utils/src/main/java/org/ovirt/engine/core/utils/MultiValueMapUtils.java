package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class for manipulating maps that have a value of a {@link List} type.
 */
public class MultiValueMapUtils {

    /** A private constructor to prohibit instantiation */
    private MultiValueMapUtils() {
    }

    /**
     * Adds an additional {@link #value} to the given {@link #key} in the {@link #map}.
     *
     * If a list of values was already associated with this key, the new value is added to its end.
     * It's assumed that the existing list implementation is mutable.
     *
     * If there is no list associated with this value, a new list containing the value will be created.
     *
     * @param key The key of the map to which the value should be added.
     * @param value The value to add to the map
     * @param map The map to have the value added
     */
    public static <K, V> void addToMap(K key, V value, Map<K, List<V>> map) {
        List<V> existingList = map.get(key);
        if (existingList == null) {
            existingList = new ArrayList<V>();
            map.put(key, existingList);
        }
        existingList.add(value);
    }
}
