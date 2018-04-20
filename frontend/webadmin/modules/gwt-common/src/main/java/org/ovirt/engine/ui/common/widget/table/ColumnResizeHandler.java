package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.common.widget.table.header.ResizableHeader;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * Handles mouse move events during column resize operation, updating column width according to current mouse position.
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnResizeHandler<T> implements NativePreviewHandler {

    //The offset to stop the header from jumping when starting to drag.
    private static final int DRAG_AREA_OFFSET = ResizableHeader.RESIZE_BAR_WIDTH / 2 + 1;

    private final Element headerElement;
    private final Column<T, ?> column;
    private final HasResizableColumns<T> table;
    private final int columnAbsoluteLeft;

    // Used to release native event handler after we are done with column resizing
    private final HandlerRegistration eventHandler;

    public ColumnResizeHandler(Element headerElement, Column<T, ?> column, HasResizableColumns<T> table) {
        this.headerElement = headerElement;
        this.column = column;
        this.table = table;
        this.eventHandler = Event.addNativePreviewHandler(this);

        this.columnAbsoluteLeft = headerElement.getAbsoluteLeft();
        // Indicate resize start
        table.onResizeStart(column, headerElement);
    }

    @Override
    public void onPreviewNativeEvent(NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        nativeEvent.preventDefault();
        nativeEvent.stopPropagation();

        if (BrowserEvents.MOUSEMOVE.equals(nativeEvent.getType())) {
            // Calculate display-relative column width
            int clientX = nativeEvent.getClientX();
            int displayColumnWidth = clientX - columnAbsoluteLeft + DRAG_AREA_OFFSET;

            // Adjust column width as necessary
            int minimumColumnWidth = table.getMinimumColumnWidth(column);
            displayColumnWidth = displayColumnWidth < minimumColumnWidth ? minimumColumnWidth : displayColumnWidth;

            // Resize the column
            table.resizeColumn(column, displayColumnWidth);
        } else if (BrowserEvents.MOUSEUP.equals(nativeEvent.getType())) {
            // Release native event handler
            eventHandler.removeHandler();

            // Indicate resize end
            table.onResizeEnd(column, headerElement);
        }
    }

}
