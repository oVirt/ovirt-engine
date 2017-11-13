package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.user.cellview.client.Column;

/**
 * Contract for table widgets that support controlling their columns in terms of visibility and positioning.
 *
 * @param <T>
 *            Table row data type.
 */
public interface ColumnController<T> {

    int NO_DRAG = -1;

    /**
     * Get column at the given index.
     * <p>
     * Throws {@link IndexOutOfBoundsException} if the column is not present.
     */
    Column<T, ?> getColumn(int index);

    /**
     * Get index of the given column.
     * <p>
     * Returns {@code -1} if the column is not present.
     */
    int getColumnIndex(Column<T, ?> column);

    /**
     * Get title of the given column to display in the context menu.
     * <p>
     * Returns {@code null} if the column is not present.
     */
    String getColumnContextMenuTitle(Column<T, ?> column);

    /**
     * Get visibility of the given column.
     * <p>
     * Returns {@code false} if the column is not present.
     */
    boolean isColumnVisible(Column<T, ?> column);

    /**
     * Set visibility of the given column.
     * <p>
     * Does nothing if the column is not present.
     */
    void setColumnVisible(Column<T, ?> column, boolean visible);

    /**
     * Persist the visible value.
     * @param column The column to persist the visibility of.
     * @param The visibility of the column.
     */
    void persistColumnVisibility(Column<T, ?> column, boolean visible);

    /**
     * Swap position of given columns.
     * <p>
     * Does nothing unless both columns are present.
     */
    void swapColumns(Column<T, ?> columnOne, Column<T, ?> columnTwo);

    /**
     * Get index of the menu item currently being dragged.
     */
    int getDragIndex();

    /**
     * Set index of the menu item currently being dragged.
     */
    void setDragIndex(int dragIndex);

    /**
     * Force update of all context menu items.
     */
    void updateColumnContextMenu();

}
