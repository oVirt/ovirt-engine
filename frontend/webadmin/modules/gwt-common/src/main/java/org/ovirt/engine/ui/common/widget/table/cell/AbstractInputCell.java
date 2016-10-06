package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.tooltip.ProvidesTooltipForObject;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

/**
 * Base class for all Cells that would otherwise extend GWT AbstractInputCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 */
public abstract class AbstractInputCell<C, V> extends com.google.gwt.cell.client.AbstractInputCell<C, V> implements Cell<C>,
        ProvidesTooltipForObject<C> {

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;

    private SafeHtml tooltipFallback;

    @Override
    public Set<String> getConsumedEvents() {
        return new HashSet<>(ElementTooltipUtils.HANDLED_CELL_EVENTS);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, C value, NativeEvent event, ValueUpdater<C> valueUpdater) {
        ElementTooltipUtils.handleCellEvent(event, parent, getTooltip(value));
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

    /**
     * Let the Cell render the tooltip using C value. This is only attempted if the Column itself
     * did not provide a tooltip. This is usually only used when there is a Composite Column that
     * contains multiple Cells, but each Cell needs its own tooltip.
     */
    public SafeHtml getTooltip(C value) {
        return tooltipFallback;
    }

    @Override
    public void setTooltipFallback(SafeHtml tooltipFallback) {
        this.tooltipFallback = tooltipFallback;
    }

    /**
     * Override the normal render to pass along an id.
     *
     * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
     */
    @Override
    public final void render(Context context, C value, SafeHtmlBuilder sb) {
        String id = ElementIdUtils.createTableCellElementId(getElementIdPrefix(), getColumnId(), context);
        render(context, value, sb, id);
    }

    /**
     * Render the cell. Using the value, the id, and the context, append some HTML to the
     * SafeHtmlBuilder that will show in the cell when it is rendered.
     *
     * Override this and use the id in your render.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder, java.lang.String)
     */
    public abstract void render(Context context, C value, SafeHtmlBuilder sb, String id);

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getElementIdPrefix() {
        return elementIdPrefix;
    }

    public String getColumnId() {
        return columnId;
    }

}
