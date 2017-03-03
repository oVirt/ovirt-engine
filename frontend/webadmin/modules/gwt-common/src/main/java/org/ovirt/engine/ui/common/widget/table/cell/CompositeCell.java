package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.tooltip.ProvidesTooltipForObject;

import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
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
public class CompositeCell<C> extends com.google.gwt.cell.client.CompositeCell<C> implements Cell<C>,
        ProvidesTooltipForObject<C> {

    private final List<HasCell<C, ?>> hasCells;

    // DOM element ID settings for text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    private SafeHtml tooltipFallback;

    public CompositeCell(List<HasCell<C, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
    }

    @Override
    public Set<String> getConsumedEvents() {
        return new HashSet<>(ElementTooltipUtils.HANDLED_CELL_EVENTS);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, C value,
            NativeEvent event, ValueUpdater<C> valueUpdater) {
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

    // TODO-GWT: copied from GWT CompositeCell, with ID injected. Keep in sync on GWT upgrade.
    protected <X> void render(Context context, C value, SafeHtmlBuilder sb, HasCell<C, X> hasCell, String id) {
        com.google.gwt.cell.client.Cell<X> _cell = hasCell.getCell();
        if (_cell instanceof Cell) {
            Cell<X> cell = (Cell<X>) _cell; // cast from GWT Cell to our Cell impl
            sb.appendHtmlConstant("<span id=\"" + id + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            cell.render(context, hasCell.getValue(value), sb, id);
            sb.appendHtmlConstant("</span>"); //$NON-NLS-1$
        } else {
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
