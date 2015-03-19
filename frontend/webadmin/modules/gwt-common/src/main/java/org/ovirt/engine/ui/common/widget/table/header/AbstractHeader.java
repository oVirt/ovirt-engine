package org.ovirt.engine.ui.common.widget.table.header;

import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
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

    public AbstractHeader(Cell<H> cell) {
        super(cell);
    }

    public Cell<H> getCell() {
        return (Cell<H>) super.getCell();
    }

    /**
     * This is copied from GWT's Header, but we also inject the tooltip content into the cell.
     * TODO-GWT: make sure that this method is in sync with Header::onBrowserEvent.
     */
    public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
        getCell().onBrowserEvent(context, elem, getValue(), getTooltip(), event, updater);
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    public void setUpdater(ValueUpdater<H> updater) {
        this.updater = updater;
    }

}
