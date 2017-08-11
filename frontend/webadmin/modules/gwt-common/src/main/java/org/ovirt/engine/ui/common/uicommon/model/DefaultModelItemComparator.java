package org.ovirt.engine.ui.common.uicommon.model;

import java.util.Comparator;
import java.util.function.Function;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;

/**
 * Default {@link Comparator} to use when rendering list model items.
 */
public class DefaultModelItemComparator<T> implements Comparator<T> {

    /**
     * Create new {@link Comparator} that combines {@code actualComparator}
     * with {@link DefaultModelItemComparator} as the fallback.
     */
    public static <T> Comparator<T> fallbackFor(Comparator<T> actualComparator) {
        return actualComparator.thenComparing(new DefaultModelItemComparator<>());
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
        return (a, b) -> {
            String name1 = (a instanceof Nameable) ? ((Nameable) a).getName() : null;
            String name2 = (b instanceof Nameable) ? ((Nameable) b).getName() : null;
            return LexoNumericComparator.comp(name1, name2);
        };
    }

    private Comparator<T> createIdComparator() {
        return Comparator.comparing((Function<T, Comparable>) item -> {
            Object id = (item instanceof Queryable) ? ((Queryable) item).getQueryableId() : null;
            return (id instanceof Comparable) ? (Comparable) id : null;
        }, Comparator.nullsLast(Comparator.naturalOrder()));
    }

}
