package org.ovirt.engine.ui.common.widget.table.header;

import org.ovirt.engine.ui.common.widget.table.NativeContextMenuHandler;
import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;

/**
 * A {@link Header}. Supports tooltips. Supports element-id framework.
 * <em>All oVirt table headers should extend this.</em>
 *
 * @param <H> Cell data type.
 */
public abstract class AbstractHeader<H> extends Header<H> implements ColumnWithElementId, TooltipHeader {

    private ValueUpdater<H> updater = null;

    private NativeContextMenuHandler contextMenuHandler;

    public AbstractHeader(Cell<H> cell) {
        super(cell);
    }

    @Override
    public Cell<H> getCell() {
        return (Cell<H>) super.getCell();
    }

    /**
     * This is copied from GWT's Header, but we also inject the tooltip content into the cell.
     * TODO-GWT: make sure that this method is in sync with Header::onBrowserEvent.
     */
    @Override
    public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
        getCell().onBrowserEvent(context, elem, getValue(), getTooltip(), event, updater);

        if (BrowserEvents.CONTEXTMENU.equals(event.getType()) && contextMenuHandler != null) {
            contextMenuHandler.onContextMenu(event);
        }
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public void setUpdater(ValueUpdater<H> updater) {
        this.updater = updater;
    }

    public void setContextMenuHandler(NativeContextMenuHandler contextMenuHandler) {
        this.contextMenuHandler = contextMenuHandler;
    }

}
