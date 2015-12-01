package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

/**
 * <p>
 * Base class for all Cells that would otherwise extend GWT AbstractCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 * Support tooltips. Note that for a Composite cell, you may want tooltips on each component
 * of the cell. In that case, do not implement getTooltip() in this cell's Column. Instead, implement
 * it in each component Cell. (The downside of this is that you have no access to the row type, so
 * you cannot make a component tooltip be a property of that row type. For example, you cannot have
 * a component tooltip be a VM's name. It must be a constant.)
 * </p>
 * @param <C> Cell data type.
 */
public class CompositeCell<C> extends com.google.gwt.cell.client.CompositeCell<C> implements Cell<C> {

    private final List<HasCell<C, ?>> hasCells;

    // DOM element ID settings for text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    public CompositeCell(List<HasCell<C, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
    }

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
     * Handle events for this cell.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, com.google.gwt.dom.client.Element, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtml, com.google.gwt.dom.client.NativeEvent, com.google.gwt.cell.client.ValueUpdater)
     */
    public void onBrowserEvent(Context context, Element parent, C value,
            SafeHtml tooltipContent, NativeEvent event, ValueUpdater<C> valueUpdater) {

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

        super.onBrowserEvent(context, parent, value, event, valueUpdater);
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
    public final void render(Context context, C value, SafeHtmlBuilder sb) {
        String id = ElementIdUtils.createTableCellElementId(getElementIdPrefix(), getColumnId(), context);
        render(context, value, sb, id);
    }

    /**
     * Override the normal render just to prevent anyone from calling it.
     */
    protected final <X> void render(Context context, C value, SafeHtmlBuilder sb, HasCell<C, X> hasCell) {
        throw new IllegalStateException("Please do not call this overload of render(). Use the overload with an id."); //$NON-NLS-1$
    }

    /**
     * Render the cell. Using the value, the id, and the context, append some HTML to the
     * SafeHtmlBuilder that will show in the cell when it is rendered.
     *
     * Override this and use the id in your render.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder, java.lang.String)
     */
    public void render(Context context, C value, SafeHtmlBuilder sb, String id) {
        int i = 1;
        for (HasCell<C, ?> hasCell : hasCells) {
            render(context, value, sb, hasCell, id + "_" + i); //$NON-NLS-1$
            i++;
        }
    }

    /**
     * TODO-GWT: copied from CompositeCell, with id injected. Keep in sync on GWT upgrades.
     */
    protected <X> void render(Context context, C value, SafeHtmlBuilder sb, HasCell<C, X> hasCell, String id) {
        com.google.gwt.cell.client.Cell<X> _cell = hasCell.getCell();
        if (_cell instanceof Cell) {
            Cell<X> cell = (Cell<X>) _cell; // cast from GWT Cell to our Cell impl
            sb.appendHtmlConstant("<span id=\"" + id + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            cell.render(context, hasCell.getValue(value), sb, id);
            sb.appendHtmlConstant("</span>"); //$NON-NLS-1$
        }
        else {
            throw new IllegalStateException("CompositeCell cannot render Cells that do not implement " //$NON-NLS-1$
                    + Cell.class.getName());
        }
    }

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
