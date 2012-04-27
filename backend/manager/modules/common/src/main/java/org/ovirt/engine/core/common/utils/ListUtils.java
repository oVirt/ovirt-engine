package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for list-manipulation.
 * Inspired by commons-collections but with java 1.5 generics.
 */
public class ListUtils {
    /**
     * A generic interface that can be used to filter search results.
     * @param <T>
     *            type parameter for the list to be filtered
     */
    public interface Filter<T> {
        /**
         * Filter the list.
         *
         * @param data
         * @return filtered results or null
         */
        List<T> filter(List<T> data);
    }

    public interface Predicate<T> {
        boolean evaluate(T obj);
    }

    /**
     * Creates a filteres list with the elements evaluated to <strong>true</strong>.
     *
     * @param <T>
     */
    public static class PredicateFilter<T> implements Filter<T> {

        public PredicateFilter(Predicate<T> predicate) {
            super();
            this.predicate = predicate;
        }

        final Predicate<T> predicate;

        @Override
        public List<T> filter(final List<T> data) {
            final ArrayList<T> ret = new ArrayList<T>();
            for (final T obj : data) {
                if (predicate.evaluate(obj)) {
                    ret.add(obj);
                }
            }
            return ret;
        }

    }

    /**
     * Filter a list.
     * @param list  the list to filter
     * @param filter the filter to use
     * @return the filtered list or null if the original list was null
     */
    public static <T> List<T> filter(final List<T> list, final Filter<T> filter) {
        if (list == null) {
            return null;
        } else {
            return filter == null ? list : filter.filter(list);
        }
    }

    public static <T> void nullSafeAdd(List<T> list, T elem) {
        if (list != null) {
            list.add(elem);
        }
    }

    public static <T> void nullSafeElemAdd(List<T> list, T elem) {
        if (elem != null) {
            nullSafeAdd(list, elem);
        }
    }

}
