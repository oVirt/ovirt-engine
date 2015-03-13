package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;

import com.google.gwt.user.cellview.client.Column;

/**
 * Base class for Columns that work with Cells that use the Element ID framework.
 * Supports sorting (setting options for server-side and client-side sorting).
 * Supports element-id framework.
 */
public abstract class AbstractColumn<T, C> extends Column<T, C> implements ColumnWithElementId, SortableColumn<T, C> {

    // Name of the field to sort by, or null for undefined sort order
    // (applies in case of server-side sorting)
    private String sortBy;

    // Comparator for sorting table row data objects, or null for undefined sorting
    // (applies in case of client-side sorting)
    private Comparator<? super T> comparator;

    public AbstractColumn(CellWithElementId<C> cell) {
        super(cell);
    }

    public CellWithElementId<C> getCell() {
        return (CellWithElementId<C>) super.getCell();
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.column.SortableColumn#makeSortable(java.lang.String)
     */
    @Override
    public void makeSortable(String sortBy) {
        assert sortBy != null : "sortBy cannot be null"; //$NON-NLS-1$
        this.sortBy = sortBy;
        this.comparator = null;
        setSortable(true);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.column.SortableColumn#makeSortable(java.util.Comparator)
     */
    @Override
    public void makeSortable(Comparator<? super T> comparator) {
        assert comparator != null : "comparator cannot be null"; //$NON-NLS-1$
        this.sortBy = null;
        this.comparator = comparator;
        setSortable(true);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.column.SortableColumn#getSortBy()
     */
    @Override
    public String getSortBy() {
        return sortBy;
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.column.SortableColumn#getComparator()
     */
    @Override
    public Comparator<? super T> getComparator() {
        return comparator;
    }

}
