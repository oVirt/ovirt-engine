package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.TooltipCell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;

/**
 * A {@link Column}. Supports tooltips.
 * TODO: add sorting support. Add element-id support. (added in patch 3 of 12)
 *
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Cell data type.
 */
public abstract class AbstractColumn<T, C> extends Column<T, C> implements ColumnWithElementId {

    public AbstractColumn(TooltipCell<C> cell) {
        super(cell);
    }

    public TooltipCell<C> getCell() {
        return (TooltipCell<C>) super.getCell();
    }

    /**
     * <p>
     * Implement this to return tooltip content for T object. You'll likely use some member(s)
     * of T to build a tooltip. You could also use a constant if the tooltip is always the same
     * for this column.
     * </p>
     * <p>
     * The tooltip cell will then use this value when rendering the cell.
     * </p>
     *
     * @param object
     * @return tooltip content
     */
    public abstract SafeHtml getTooltip(T object);

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

}
