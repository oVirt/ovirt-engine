package org.ovirt.engine.core.utils.linq;

import java.util.Collection;
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
}
