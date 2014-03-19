package org.ovirt.engine.ui.common.widget.table.resize;

import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlCellWithTooltip;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.TableCellElement;
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
    private final HasResizableColumns<T> table;

    public ResizableHeader(SafeHtml text, Column<T, ?> column, HasResizableColumns<T> table) {
        this(text, column, table, new SafeHtmlCellWithTooltip(
                "click", "mousedown", "mousemove", "mouseover")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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

        int clientX = event.getClientX();
        int absoluteLeft = target.getAbsoluteLeft();
        int offsetWidth = target.getOffsetWidth();
        boolean mouseOverResizeBarArea = clientX > absoluteLeft + offsetWidth - RESIZE_BAR_WIDTH;

        // Resolve th element (header cell for given column)
        Element headerElement = findThElement(target);
        assert headerElement != null;

        // Update mouse cursor for the header element
        if (mouseOverResizeBarArea) {
            headerElement.getStyle().setCursor(Cursor.COL_RESIZE);
        } else if (column.isSortable()) {
            headerElement.getStyle().setCursor(Cursor.POINTER);
        } else {
            headerElement.getStyle().setCursor(Cursor.DEFAULT);
        }

        // On mouse down event, which initiates the column resize operation,
        // register a column resize handler that listens to mouse move events
        if ("mousedown".equals(event.getType())) { //$NON-NLS-1$
            if (mouseOverResizeBarArea) {
                new ColumnResizeHandler<T>(headerElement, column, table);
            }
            event.preventDefault();
            event.stopPropagation();
        }
    }

    Element findThElement(Element elm) {
        if (elm == null) {
            return null;
        } else if (TableCellElement.TAG_TH.equalsIgnoreCase(elm.getTagName())) {
            return elm;
        }
        return findThElement(elm.getParentElement());
    }

}
