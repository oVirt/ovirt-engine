package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SortedListModel<T> extends ListModel<T> {

    static class SortSensitiveComparator<T> implements Comparator<T> {

        private final Comparator<? super T> comparator;
        private final boolean sortAscending;

        SortSensitiveComparator(Comparator<? super T> comparator, boolean sortAscending) {
            assert comparator != null : "comparator cannot be null"; //$NON-NLS-1$
            this.comparator = comparator;
            this.sortAscending = sortAscending;
        }

        @Override
        public int compare(T a, T b) {
            return sortAscending ? comparator.compare(a, b) : comparator.compare(b, a);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof SortSensitiveComparator)) {
                return false;
            }
            SortSensitiveComparator<?> other = (SortSensitiveComparator<?>) obj;
            return Objects.equals(comparator, other.comparator)
                    && sortAscending == other.sortAscending;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    comparator,
                    sortAscending
            );
        }

    }

    protected Comparator<T> comparator;

    public SortedListModel(Comparator<? super T> comparator) {
        super();
        setComparator(comparator);
    }

    public SortedListModel() {
        this(null);
    }

    public final void setComparator(Comparator<? super T> comparator) {
        setComparator(comparator, true);
    }

    public void setComparator(Comparator<? super T> comparator, boolean sortAscending) {
        this.comparator = (comparator != null) ? new SortSensitiveComparator<>(comparator, sortAscending) : null;
    }

    @Override
    public void setItems(Collection<T> value) {
        Collection<T> sortedItems = sortItems(value);
        super.setItems(sortedItems);
    }

    protected final Collection<T> sortItems(Collection<T> items) {
        if (items == null || comparator == null) {
            return items;
        }

        List<T> sortedList = new ArrayList<>(items);
        Collections.sort(sortedList, comparator);

        return sortedList;
    }

}
