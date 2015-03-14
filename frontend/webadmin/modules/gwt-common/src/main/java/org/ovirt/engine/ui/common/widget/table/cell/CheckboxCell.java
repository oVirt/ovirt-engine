package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;

/**
 * <p>
 * Base class for all Cells that would otherwise extend GWT CheckboxCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 * </p>
 */
public class CheckboxCell extends com.google.gwt.cell.client.CheckboxCell implements Cell<Boolean> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<input id=\"{0}\" type=\"checkbox\" tabindex=\"-1\" checked/>")
        SafeHtml inputChecked(String id);

        @Template("<input id=\"{0}\" type=\"checkbox\" tabindex=\"-1\" />")
        SafeHtml inputUnchecked(String id);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;

    public CheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
        super(dependsOnSelection, handlesSelection);
    }

    /**
     * Override the normal render to pass along an id.
     *
     * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
     */
    public final void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        String id = ElementIdUtils.createTableCellElementId(getElementIdPrefix(), getColumnId(), context);
        render(context, value, sb, id);
    }

    /**
     * Render the cell. Using the value, the id, and the context, append some HTML to the
     * SafeHtmlBuilder that will show in the cell when it is rendered.
     *
     * Override this and use the id in your render.
     *
     * TODO-GWT: this is copied from GWT's CheckboxCell, with ID injected. Keep in sync on GWT upgrade.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder, java.lang.String)
     */
    public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
        // Get the view data.
        Object key = context.getKey();
        Boolean viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        if (value != null && ((viewData != null) ? viewData : value)) {
            sb.append(templates.inputChecked(id));
        } else {
            sb.append(templates.inputUnchecked(id));
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

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent,
            Boolean value, SafeHtml tooltipContent, NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

}
