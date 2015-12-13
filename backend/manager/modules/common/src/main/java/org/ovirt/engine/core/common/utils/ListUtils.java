package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
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
}
