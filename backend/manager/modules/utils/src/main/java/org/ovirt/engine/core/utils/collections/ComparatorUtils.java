package org.ovirt.engine.core.utils.collections;

import java.util.Comparator;
import java.util.Objects;

public class ComparatorUtils {

    /**
     * Comparator that doesn't change order of elements (with stable sorting algorithm) except for {@code element}
     * that is sorted as the last one.
     */
    public static <T> Comparator<T> sortLast(final T element) {
        return (a, b) -> {
            if (Objects.equals(a, b)) {
                return 0;
            }
            if (element.equals(a)) {
                return 1;
            }
            if (element.equals(b)) {
                return -1;
            }
            return 0;
        };
    }
}
