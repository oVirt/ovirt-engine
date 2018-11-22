package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListUtils {
    /**
     * Computes the ranks of objects in a list.
     * Rank is the number of elements that are less than the current element.
     *
     * @param objects    Sorted list of objects according to comparator
     * @param comparator Comparator used for sorting
     * @return  List of ranks.
     */
    public static<T> List<Integer> rankSorted(List<? extends T> objects, Comparator<T> comparator) {
        if (objects.isEmpty()) {
            return Collections.emptyList();
        }

        int currentRank = 0;
        int realRank = 0;
        List<Integer> result = new ArrayList<>(objects.size());

        T lastObject = null;
        for (T object : objects) {
            if (lastObject == null || comparator.compare(object, lastObject) > 0) {
                currentRank = realRank;
                lastObject = object;
            }
            realRank++;

            result.add(currentRank);
        }

        return result;
    }
}
