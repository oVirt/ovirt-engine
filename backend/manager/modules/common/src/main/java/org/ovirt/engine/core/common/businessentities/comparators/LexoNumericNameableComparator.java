package org.ovirt.engine.core.common.businessentities.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Nameable;

public class LexoNumericNameableComparator<T extends Nameable> implements Comparator<T> {
    private boolean caseSensitive;

    public LexoNumericNameableComparator(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public LexoNumericNameableComparator() {
        this(false);
    }

    @Override
    public int compare(T o1, T o2) {
        return LexoNumericComparator.comp(o1.getName(), o2.getName(), caseSensitive);
    }
}
