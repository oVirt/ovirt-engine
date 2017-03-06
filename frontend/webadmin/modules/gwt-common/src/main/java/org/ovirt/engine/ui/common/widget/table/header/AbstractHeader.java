package org.ovirt.engine.ui.common.widget.table.header;

import org.ovirt.engine.ui.common.widget.table.NativeContextMenuHandler;
import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;
import org.ovirt.engine.ui.common.widget.tooltip.ProvidesTooltip;

import com.google.gwt.cell.client.Cell.Context;
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
public abstract class AbstractHeader<H> extends Header<H> implements ColumnWithElementId, ProvidesTooltip {

    private NativeContextMenuHandler contextMenuHandler;

    public AbstractHeader(Cell<H> cell) {
        super(cell);
    }

    @Override
    public Cell<H> getCell() {
        return (Cell<H>) super.getCell();
    }

    /**
     * Override to inject the tooltip content into the cell.
     */
    @Override
    public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
        super.onBrowserEvent(context, elem, event);

        if (BrowserEvents.CONTEXTMENU.equals(event.getType()) && contextMenuHandler != null) {
            contextMenuHandler.onContextMenu(event);
        }
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    public void setContextMenuHandler(NativeContextMenuHandler contextMenuHandler) {
        this.contextMenuHandler = contextMenuHandler;
    }

}
