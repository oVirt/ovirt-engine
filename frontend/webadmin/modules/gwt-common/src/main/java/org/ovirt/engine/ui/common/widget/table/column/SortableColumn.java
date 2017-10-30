package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

/**
 * Represents a column that allows setting options for server-side and client-side sorting.
 *
 * @param <T> Row data type.
 */
public interface SortableColumn<T> {

    /**
     * Enables <em>server-side</em> sorting for this column.
     *
     * @param sortBy
     *            Name of the field to sort by, used within the search query.
     */
    void makeSortable(String sortBy);

    /**
     * Enables <em>client-side</em> sorting for this column.
     *
     * @param comparator
     *            Comparator for sorting table row data objects.
     */
    void makeSortable(Comparator<? super T> comparator);

    /**
     * Returns the name of the field to sort by, or {@code null} for undefined sort order.
     * <p>
     * Use this method if server-side sorting is supported by the underlying model.
     */
    String getSortBy();

    /**
     * Returns the {@link Comparator} for sorting table row data objects, or {@code null} for undefined sorting.
     * <p>
     * Use this method if server-side sorting is <em>not</em> supported by the underlying model.
     */
    Comparator<? super T> getComparator();

}
