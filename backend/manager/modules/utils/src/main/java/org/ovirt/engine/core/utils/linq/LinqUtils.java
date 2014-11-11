package org.ovirt.engine.core.utils.linq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LinqUtils {

    /**
     * Replaces firstOrDefault() LINQ
     *
     * Returns first object from the collection that matches the predicate, or null if no such object was found
     */
    public static <T> T firstOrNull(Collection<T> collection, Predicate<T> predicate) {
        for (T t : collection) {
            if (predicate.eval(t))
                return t;
        }

        return null;
    }

    /**
     * Replaces first() LINQ
     *
     * Returns first object of collection or null if empty
     */
    public static <T> T first(Collection<T> collection) {
        Iterator<T> iterator = collection.iterator();
        if (iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    /**
     * Replaces LINQ's select()
     *
     * Returns a new list which contains all the objects from original collection with function applied to them.
     */
    public static <IN, OUT> List<OUT> transformToList(final Collection<IN> collection, final Function<IN, OUT> f) {
        LinkedList<OUT> list = new LinkedList<OUT>();
        for (IN in : collection) {
            list.add(f.eval(in));
        }
        return list;
    }

    /**
     * Replaces LINQ WHERE
     *
     * Returns list containing all objects from original collection that matches the predicate
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        LinkedList<T> results = new LinkedList<T>();
        for (T t : collection) {
            if (predicate.eval(t))
                results.add(t);
        }

        return results;
    }

    /**
     * Replaces LINQ ToDictionary()
     *
     * Transforms given collection into map using mapper
     * <p>
     *
     * See DefaultMapper if you want to map keys only (similar to single parameter toDictionary())
     */
    public static <IN, KEY, VALUE> Map<KEY, VALUE> toMap(Collection<IN> collection, Mapper<IN, KEY, VALUE> mapper) {
        Map<KEY, VALUE> map = new LinkedHashMap<KEY, VALUE>();
        for (IN in : collection) {
            map.put(mapper.createKey(in), mapper.createValue(in));
        }

        return map;
    }

    /**
     * Maps the input collection by the given mapper and groups the result values by key.
     * Similar to {@link #toMap} method but allows multiple values to be mapped to the same key with no loss of data.
     *
     * @param collection the input collection
     * @param mapper the mapper
     * @param <IN> type of the input collection
     * @param <KEY> key type of the output Map
     * @param <VALUE> value type of the output Map
     * @return the transformed Map
     */
    public static <IN, KEY, VALUE> Map<KEY, List<VALUE>> toMultiMap(Collection<IN> collection, Mapper<IN, KEY, VALUE> mapper) {
        final Map<KEY, List<VALUE>> map = new HashMap<>();
        for (IN in : collection) {
            final KEY key = mapper.createKey(in);
            final List<VALUE> values;
            if (map.containsKey(key)) {
                values = map.get(key);
            } else {
                values = new ArrayList<>();
                map.put(key, values);
            }
            values.add(mapper.createValue(in));
        }

        return map;
    }

    public static <VALUE> VALUE aggregate(Collection<VALUE> values, Aggregator<VALUE> aggregator) {
        boolean first = true;
        VALUE aggregate = null;

        for (VALUE value : values) {
            if (first) {
                aggregate = value;
                first = false;
            } else {
                aggregate = aggregator.process(aggregate, value);
            }
        }
        return aggregate;
    }

    /**
     * Creates the opposite {@link Predicate} to the given one.
     *
     * @param predicate the given {@link Predicate}.
     * @param <T>       the type of the objects the {@link Predicate} deals with.
     * @return the opposite {@link Predicate} to the given one.
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return new NotPredicate<>(predicate);
    }

    /**
     * Concatenates the input collections into a single {@link Collection}.
     * Note: The change to the result {@link Collection} has no impact on the input collections.
     *
     * @param collections the input collections.
     * @return the new concatenated {@link Collection}.
     */
    public static <T> Collection<T> concat(Collection<? extends T>... collections) {
        final List<T> result = new LinkedList<>();
        for (Collection<? extends T> collection : collections) {
            result.addAll(collection);
        }
        return result;
    }
}
