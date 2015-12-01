package org.ovirt.engine.core.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * If a collection of values was already associated with this key, the new value is added to its end.
     * It's assumed that the existing list implementation is mutable.
     *
     * If there is no collection associated with this value, a new collection containing the value will be created.
     *
     * @param key The key of the map to which the value should be added.
     * @param value The value to add to the map
     * @param map The map to have the value added
     * @param creator A factory to create the new (empty) collection for the values to be held
     */
    public static <K, V, C extends Collection<V>> void addToMap(K key,
            V value,
            Map<K, C> map,
            CollectionCreator<V, C> creator) {
        C existingValues = map.get(key);
        if (existingValues == null) {
            existingValues = creator.create();
            map.put(key, existingValues);
        }
        existingValues.add(value);
    }

    /**
     * Same as {@link #addToMap(Object, Object, Map, CollectionCreator)}, with the default {@link ArrayList} to hold the values.
     */
    public static <K, V> void addToMap(K key, V value, Map<K, List<V>> map) {
        addToMap(key, value, map, new ListCreator<>());
    }

    public static <K, V> void addToMapOfSets(K key, V value, Map<K, Set<V>> map) {
        addToMap(key, value, map, new SetCreator<>());
    }

    public static interface CollectionCreator<V, C extends Collection<V>> {
        C create();
    }

    public static class ListCreator<V> implements CollectionCreator<V, List<V>> {
        @Override
        public List<V> create() {
            return new ArrayList<>();
        }
    }

    public static class ArrayListCreator<V> implements CollectionCreator<V, ArrayList<V>> {
        @Override
        public ArrayList<V> create() {
            return new ArrayList<>();
        }
    }

    public static class LinkedListCreator<V> implements CollectionCreator<V, LinkedList<V>> {
        @Override
        public LinkedList<V> create() {
            return new LinkedList<>();
        }
    }

    public static class SetCreator<V> implements CollectionCreator<V, Set<V>> {
        @Override
        public Set<V> create() {
            return new HashSet<>();
        }
    }

    public static class HashSetCreator<V> implements CollectionCreator<V, HashSet<V>> {
        @Override
        public HashSet<V> create() {
            return new HashSet<>();
        }
    }

    public static class LinkedHashSetCreator<V> implements CollectionCreator<V, Set<V>> {
        @Override
        public Set<V> create() {
            return new LinkedHashSet<>();
        }
    }

    public static <K, V, C extends Collection<V>> boolean removeFromMap(Map<K, C> map, K key, V value) {
        C collection = map.get(key);
        if (collection == null) {
            return false;
        }
        boolean success = collection.remove(value);
        if (success && collection.size() == 0) {
            return map.remove(key) != null;
        }
        return success;

    }

}
