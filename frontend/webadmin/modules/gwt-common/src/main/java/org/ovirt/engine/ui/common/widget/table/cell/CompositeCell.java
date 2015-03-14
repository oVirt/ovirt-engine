package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.List;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

/**
 * <p>
 * Base class for all Cells that would otherwise extend GWT AbstractCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 * </p>
 * @param <C> Cell data type.
 */
public class CompositeCell<C> extends com.google.gwt.cell.client.CompositeCell<C> implements CellWithElementId<C> {

    private final List<HasCell<C, ?>> hasCells;

    // DOM element ID settings for text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    public CompositeCell(List<HasCell<C, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
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
     * Render the cell. Using the value, the id, and the context, append some HTML to the
     * SafeHtmlBuilder that will show in the cell when it is rendered.
     *
     * Override this and use the id in your render.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.TooltipCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder, java.lang.String)
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
        Cell<X> cell = hasCell.getCell();
        sb.appendHtmlConstant("<span id=\"" + id + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        cell.render(context, hasCell.getValue(value), sb);
        sb.appendHtmlConstant("</span>"); //$NON-NLS-1$
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
