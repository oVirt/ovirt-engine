package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;

/**
 * Base class for Columns that work with Cells that use the Element ID framework.
 * Supports sorting (setting options for server-side and client-side sorting).
 * Supports element-id framework.
 * Supports tooltips.
 *
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Cell data type.
 */
public abstract class AbstractColumn<T, C> extends Column<T, C> implements ColumnWithElementId, TooltipColumn<T>, SortableColumn<T, C> {

    // Name of the field to sort by, or null for undefined sort order
    // (applies in case of server-side sorting)
    private String sortBy;

    // Comparator for sorting table row data objects, or null for undefined sorting
    // (applies in case of client-side sorting)
    private Comparator<? super T> comparator;

    // Custom title for use with ColumnContextMenu (optional)
    private String contextMenuTitle;

    public AbstractColumn(Cell<C> cell) {
        super(cell);
    }

    public Cell<C> getCell() {
        return (Cell<C>) super.getCell();
    }

    /**
     * This is copied from GWT's Column, but we also inject the tooltip content into the cell.
     * TODO-GWT: make sure that this method is in sync with Column::onBrowserEvent.
     */
    public void onBrowserEvent(Context context, Element elem, final T object, NativeEvent event) {
        final int index = context.getIndex();
        ValueUpdater<C> valueUpdater = (getFieldUpdater() == null) ? null : new ValueUpdater<C>() {
            @Override
            public void update(C value) {
                getFieldUpdater().update(index, object, value);
            }
        };
        getCell().onBrowserEvent(context, elem, getValue(object), /***/ getTooltip(object) /***/, event, valueUpdater);
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

    /**
     * Default implementation of tooltip -- return null for no tooltip.
     *
     * Override this to set a tooltip for the column.
     *
     * @return the tooltip to show
     *
     * @see org.ovirt.engine.ui.common.widget.table.column.TooltipColumn#getTooltip(java.lang.Object)
     */
    @Override
    public SafeHtml getTooltip(T object) {
        return null;
    }

    public String getContextMenuTitle() {
        return contextMenuTitle;
    }

    /**
     * Sets a custom title for use with table header context menu.
     *
     * @see org.ovirt.engine.ui.common.widget.table.ColumnResizeCellTable#enableHeaderContextMenu
     */
    public void setContextMenuTitle(String contextMenuTitle) {
        this.contextMenuTitle = contextMenuTitle;
    }

}
