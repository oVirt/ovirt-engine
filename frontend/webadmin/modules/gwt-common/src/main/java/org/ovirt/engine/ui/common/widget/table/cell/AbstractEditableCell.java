package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

/**
 * <p>
 * Base class for all Cells that would otherwise extend GWT AbstractEditableCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 * </p>
 */
public abstract class AbstractEditableCell<C, V> extends com.google.gwt.cell.client.AbstractEditableCell<C, V> implements CellWithElementId<C> {

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;

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
