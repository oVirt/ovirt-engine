package org.ovirt.engine.ui.common.uicommon.model;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;

/**
 * Default {@link Comparator} to use when rendering list model items.
 */
public class DefaultModelItemComparator<T> implements Comparator<T> {

    /**
     * Create new {@link Comparator} that combines {@code actualComparator}
     * with {@link DefaultModelItemComparator} as the fallback.
     */
    public static <T> Comparator<T> fallbackFor(final Comparator<T> actualComparator) {
        return new Comparator<T>() {
            private final Comparator<T> fallbackComparator = new DefaultModelItemComparator<>();

            @Override
            public int compare(T a, T b) {
                // chain comparators like in Java 8 Comparator.thenComparing
                int res = actualComparator.compare(a, b);
                return (res != 0) ? res : fallbackComparator.compare(a, b);
            }
        };
    }

    private final Comparator<T> nameComparator;
    private final Comparator<T> idComparator;

    public DefaultModelItemComparator() {
        this.nameComparator = createNameComparator();
        this.idComparator = createIdComparator();
    }

    @Override
    public int compare(T a, T b) {
        // chain comparators like in Java 8 Comparator.thenComparing
        int res = nameComparator.compare(a, b);
        return (res != 0) ? res : idComparator.compare(a, b);
    }

    private Comparator<T> createNameComparator() {
        return new Comparator<T>() {
            @Override
            public int compare(T a, T b) {
                String name1 = (a instanceof Nameable) ? ((Nameable) a).getName() : null;
                String name2 = (b instanceof Nameable) ? ((Nameable) b).getName() : null;
                return LexoNumericComparator.comp(name1, name2);
            }
        };
    }

    private Comparator<T> createIdComparator() {
        return new Comparator<T>() {
            // inspired by Java 8 Comparators.NullComparator (nullFirst = false)
            private final Comparator<Comparable> nullComparator = new Comparator<Comparable>() {
                @Override
                public int compare(Comparable a, Comparable b) {
                    if (a == null) {
                        return (b == null) ? 0 : 1;
                    } else if (b == null) {
                        return -1;
                    } else {
                        return a.compareTo(b);
                    }
                }
            };

            @Override
            public int compare(T a, T b) {
                Object id1 = (a instanceof IVdcQueryable) ? ((IVdcQueryable) a).getQueryableId() : null;
                Object id2 = (b instanceof IVdcQueryable) ? ((IVdcQueryable) b).getQueryableId() : null;
                Comparable idComp1 = (id1 instanceof Comparable) ? (Comparable) id1 : null;
                Comparable idComp2 = (id2 instanceof Comparable) ? (Comparable) id2 : null;
                return nullComparator.compare(idComp1, idComp2);
            }
        };
    }

}
