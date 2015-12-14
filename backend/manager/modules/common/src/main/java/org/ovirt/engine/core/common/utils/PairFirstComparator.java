package org.ovirt.engine.core.common.utils;

import java.io.Serializable;
import java.util.Comparator;

public class PairFirstComparator<T, K> implements Comparator<Pair<T, K>>, Serializable {
    private static final long serialVersionUID = -6088357446855432271L;

    private Comparator<? super T> comparator;

    /** A useless no-arg constructor for GWT's sake */
    private PairFirstComparator() {
        // Don't use!
    }

    public PairFirstComparator(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(Pair<T, K> o1, Pair<T, K> o2) {
        return comparator.compare(o1.getFirst(), o2.getFirst());
    }

}
