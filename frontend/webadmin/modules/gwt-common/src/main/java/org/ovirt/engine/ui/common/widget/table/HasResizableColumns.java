package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.cellview.client.Column;

/**
 * Interface implemented by table widgets that support resizable columns.
 * <p>
 * Each column should have an explicit width defined in PX units.
 *
 * @param <T>
 *            Table row data type.
 */
public interface HasResizableColumns<T> {

    /**
     * Called when the user initiates the column resize operation.
     * <p>
     * This happens when the user depresses mouse button (mouse down event) while the cursor is within the column header
     * resize bar area.
     *
     * @param column
     *            The column being resized.
     * @param headerElement
     *            Column header element.
     */
    void onResizeStart(Column<T, ?> column, Element headerElement);

    /**
     * Called when the column resize operation finishes.
     * <p>
     * This happens when the user releases mouse button (mouse up event) after resizing given column.
     *
     * @param column
     *            The column being resized.
     * @param headerElement
     *            Column header element.
     */
    void onResizeEnd(Column<T, ?> column, Element headerElement);

    /**
     * Resizes the given column to a new width.
     *
     * @param column
     *            The column to resize.
     * @param newWidth
     *            New column width, in PX units.
     */
    void resizeColumn(Column<T, ?> column, int newWidth);

    /**
     * Returns the minimum width of the given column.
     *
     * @param column
     *            The column to search for.
     * @return Minimum width of the given column, in PX units.
     */
    int getMinimumColumnWidth(Column<T, ?> column);

}
