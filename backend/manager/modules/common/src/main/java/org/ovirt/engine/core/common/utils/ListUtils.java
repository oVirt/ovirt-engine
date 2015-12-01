package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Predicate<T> predicate;

        @Override
        public List<T> filter(final List<T> data) {
            final ArrayList<T> ret = new ArrayList<>();
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

    /**
     * Compares two lists for equality of all their elements. Returns true if both lists are of same size and every
     * element in first list has an equal in the second.
     *
     * @param firstList
     * @param secondList
     * @return
     */
    public static <T> boolean listsEqual(Collection<T> firstList, Collection<T> secondList) {
        if(firstList.size() != secondList.size()) {
            return false;
        }

        // Use set instead of the passed collection, so that complexity of contains method is o(1),
        // reducing the overall complexity of the for loop from o(n^2) to o(n)
        Set<T> second = secondList instanceof Set ? (Set<T>) secondList : new HashSet<>(secondList);
        for(T entity : firstList) {
            if(!second.contains(entity)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a collection of elements that are not present in <code>oldList</code>, but present in
     * <code>newList</code>
     *
     * @param oldCollection
     * @param newCollection
     * @return collection of elements that are not present in <code>oldList</code>, but present in <code>newList</code>
     */
    public static <T> Collection<T> getAddedElements(Collection<T> oldCollection, Collection<T> newCollection) {
        // Use set instead of the passed collection, so that complexity of contains method is o(1),
        // reducing the overall complexity of the for loop from o(n^2) to o(n)
        Set<T> old = oldCollection instanceof Set ? (Set<T>) oldCollection : new HashSet<>(oldCollection);
        List<T> addedElements = new ArrayList<>();
        for (T element : newCollection) {
            if (!old.contains(element)) {
                addedElements.add(element);
            }
        }
        return addedElements;
    }

    /**
     * @param src    The list on which we iterate to match against the lookup.
     * @param lookup The list being matched against an entry for the source.<br>
     * </br> The first match breaks the loop and is sufficient.
     * @return :
     * - the first match between a value in src against the lookup.
     * - null if the lookup is null
     * - null if there's no match
     */
    public static String firstMatch(List<String> src, String... lookup) {
        if (lookup == null) {
            return null;
        }

        Arrays.sort(lookup);
        for (String s : src) {
            int matchedIndex = Arrays.binarySearch(lookup, s);
            if (matchedIndex >= 0) {
                return lookup[matchedIndex];
            }
        }
        return null;
    }
}
