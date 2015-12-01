package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

/**
 * Base class for all Cells that would otherwise extend GWT AbstractInputCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 */
public abstract class AbstractInputCell<C, V> extends com.google.gwt.cell.client.AbstractInputCell<C, V> implements Cell<C> {

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;

    /**
     * Events to sink. By default, we only sink mouse events that tooltips need. Override this
     * (and include addAll(super.getConsumedEvents())'s events!) if your cell needs to respond
     * to additional events.
     */
    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>();
        TooltipMixin.addTooltipsEvents(set);
        return set;
    }

    /**
     * Don't call this from a custom Column. This is only used in the case this Cell is used by
     * a CompositeCell. In that case, we give each component Cell of the Composite a chance to render its own
     * tooltip.
     *
     * See userportal's composite action button cell as an example (SideTabExtendedVirtualMachineView).
     *
     * @see com.google.gwt.cell.client.AbstractCell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, com.google.gwt.dom.client.Element, java.lang.Object, com.google.gwt.dom.client.NativeEvent, com.google.gwt.cell.client.ValueUpdater)
     */
    @Override
    public final void onBrowserEvent(Context context, Element parent, C value, NativeEvent event, ValueUpdater<C> valueUpdater) {
        onBrowserEvent(context, parent, value, null, event, valueUpdater);
    }

    /**
     * Handle events for this cell.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, com.google.gwt.dom.client.Element, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtml, com.google.gwt.dom.client.NativeEvent, com.google.gwt.cell.client.ValueUpdater)
     */
    @Override
    public void onBrowserEvent(Context context, Element parent, C value, SafeHtml tooltipContent, NativeEvent event,
            ValueUpdater<C> valueUpdater) {

        // if the Column did not provide a tooltip, give the Cell a chance to render one using the cell value C
        if (tooltipContent == null) {
            tooltipContent = getTooltip(value);
        }

        if (BrowserEvents.MOUSEOVER.equals(event.getType())) {
            TooltipMixin.configureTooltip(parent, tooltipContent, event);
        }

        if (BrowserEvents.MOUSEOUT.equals(event.getType())) {
            TooltipMixin.reapAllTooltips();
        }

        if (BrowserEvents.MOUSEDOWN.equals(event.getType())) {
            TooltipMixin.hideAllTooltips();
        }
    }

    /**
     * Let the Cell render the tooltip using C value. This is only attempted if the Column itself
     * did not provide a tooltip. This is usually only used when there is a Composite Column that
     * contains multiple Cells, but each Cell needs its own tooltip.
     */
    public SafeHtml getTooltip(C value) {
        return null;
    }

    /**
     * Override the normal render to pass along an id.
     *
     * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
     */
    @Override
    public final void render(com.google.gwt.cell.client.Cell.Context context, C value, SafeHtmlBuilder sb) {
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
    public abstract void render(com.google.gwt.cell.client.Cell.Context context, C value, SafeHtmlBuilder sb, String id);

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
