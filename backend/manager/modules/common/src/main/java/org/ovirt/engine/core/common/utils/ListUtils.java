package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

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

    /**
     * Iterates over list together with ranks of each element.
     *
     * @param objects    Sorted list of objects according to comparator
     * @param comparator Comparator used for sorting
     */
    public static<T> void forEachWithRanks(List<T> objects, Comparator<? super T> comparator, BiConsumer<T, Integer> consumer) {
        List<Integer> ranks = ListUtils.rankSorted(objects, comparator);
        for (int i = 0; i < objects.size(); ++i) {
            consumer.accept(objects.get(i), ranks.get(i));
        }
    }

    /**
     * Compares two lists lexicographically
     */
    public static <T> Comparator<List<? extends T>> lexicographicListComparator(Comparator<T> comparator) {
        return (a, b) -> {
            int size1 = a.size();
            int size2 = b.size();
            int minSize = Math.min(size1, size2);

            for (int i = 0; i < minSize; i++) {
                T elem1 = a.get(i);
                T elem2 = b.get(i);
                int val = comparator.compare(elem1, elem2);
                if (val != 0) {
                    return val;
                }
            }

            return Integer.compare(size1, size2);
        };
    }

    public static <T extends Comparable<? super T>> Comparator<List<? extends T>> lexicographicListComparator() {
        return lexicographicListComparator(Comparator.<T>naturalOrder());
    }
}
