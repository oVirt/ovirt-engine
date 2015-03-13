package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;

import com.google.gwt.user.cellview.client.Column;

/**
 * A {@link Column} that allows setting options for server-side and client-side sorting.
 *
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Cell data type.
 */
public abstract class AbstractSortableColumn<T, C> extends Column<T, C> {

    // Name of the field to sort by, or null for undefined sort order
    // (applies in case of server-side sorting)
    private String sortBy;

    // Comparator for sorting table row data objects, or null for undefined sorting
    // (applies in case of client-side sorting)
    private Comparator<? super T> comparator;

    public AbstractSortableColumn(CellWithElementId<C> cell) {
        super(cell);
    }

    /**
     * Enables <em>server-side</em> sorting for this column.
     *
     * @param sortBy
     *            Name of the field to sort by, used within the search query.
     */
    public void makeSortable(String sortBy) {
        assert sortBy != null : "sortBy cannot be null"; //$NON-NLS-1$
        this.sortBy = sortBy;
        this.comparator = null;
        setSortable(true);
    }

    /**
     * Enables <em>client-side</em> sorting for this column.
     *
     * @param comparator
     *            Comparator for sorting table row data objects.
     */
    public void makeSortable(Comparator<? super T> comparator) {
        assert comparator != null : "comparator cannot be null"; //$NON-NLS-1$
        this.sortBy = null;
        this.comparator = comparator;
        setSortable(true);
    }

    /**
     * Returns the name of the field to sort by, or {@code null} for undefined sort order.
     * <p>
     * Use this method if server-side sorting is supported by the underlying model.
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Returns the {@link Comparator} for sorting table row data objects, or {@code null} for undefined sorting.
     * <p>
     * Use this method if server-side sorting is <em>not</em> supported by the underlying model.
     */
    public Comparator<? super T> getComparator() {
        return comparator;
    }

}
