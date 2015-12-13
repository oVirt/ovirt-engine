package org.ovirt.engine.core.common.utils;

import java.util.Collection;
import java.util.HashSet;
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
        return second.containsAll(firstList);
    }
}
