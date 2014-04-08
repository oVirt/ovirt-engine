package org.ovirt.engine.ui.common.widget.table.resize;

import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlCellWithTooltip;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;

/**
 * A {@link Header} that allows the user to resize the associated column by dragging its right-hand border using mouse.
 * <p>
 * This header has its value rendered through safe HTML markup.
 *
 * @param <T>
 *            Table row data type.
 */
public class ResizableHeader<T> extends Header<SafeHtml> {

    // Width of the column header resize bar area, in pixels
    private static final int RESIZE_BAR_WIDTH = 5;

    private final SafeHtml text;
    private final Column<T, ?> column;
    private Column<T, ?> previousColumn;
    private final HasResizableColumns<T> table;

    public ResizableHeader(SafeHtml text, Column<T, ?> column,
            HasResizableColumns<T> table) {
        this(text, column, table,
                new SafeHtmlCellWithTooltip("click", "mousedown", //$NON-NLS-1$ //$NON-NLS-2$
                        "mousemove", "mouseover")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public ResizableHeader(SafeHtml text, Column<T, ?> column, HasResizableColumns<T> table,
            Cell<SafeHtml> cell) {
        super(cell);
        this.text = text;
        this.column = column;
        this.table = table;
    }

    @Override
    public SafeHtml getValue() {
        return text;
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        super.onBrowserEvent(context, target, event);

        if (previousColumn == null) {
            previousColumn = table.getPreviousColumn(column);
        }
        int clientX = event.getClientX();
        int absoluteLeft = target.getAbsoluteLeft();
        int offsetWidth = target.getOffsetWidth();
        boolean mouseOverRightResizeBarArea = clientX > absoluteLeft + offsetWidth - RESIZE_BAR_WIDTH;
        boolean mouseOverLeftResizeBarArea = (clientX >= absoluteLeft && clientX < absoluteLeft + RESIZE_BAR_WIDTH)
                && previousColumn != null;

        // Update mouse cursor for the header element, using resize
        // cursor when the mouse hovers over the resize bar area
        if (mouseOverRightResizeBarArea || mouseOverLeftResizeBarArea) {
            target.getStyle().setCursor(Cursor.COL_RESIZE);
        } else {
            target.getStyle().setCursor(Cursor.DEFAULT);
        }

        // On mouse down event, which initiates the column resize operation,
        // register a column resize handler that listens to mouse move events
        if ("mousedown".equals(event.getType())) { //$NON-NLS-1$
            if (mouseOverRightResizeBarArea) {
                new ColumnResizeHandler<T>(target, column, table);
            } else if (mouseOverLeftResizeBarArea) {
                new ColumnResizeHandler<T>(target.getPreviousSiblingElement(), previousColumn, table);
            }
            event.preventDefault();
            event.stopPropagation();
        }
    }

}
