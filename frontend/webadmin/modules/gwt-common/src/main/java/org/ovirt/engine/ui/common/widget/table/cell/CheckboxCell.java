package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.tooltip.ProvidesTooltipForObject;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * <p>
 * Base class for all Cells that would otherwise extend GWT CheckboxCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 * </p>
 */
public class CheckboxCell extends com.google.gwt.cell.client.CheckboxCell implements Cell<Boolean>, EventHandlingCell,
        ProvidesTooltipForObject<Boolean> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<input id=\"{0}\" type=\"checkbox\" tabindex=\"-1\" checked style=\"{1}\"/>")
        SafeHtml inputChecked(String id, SafeStyles style);

        @Template("<input id=\"{0}\" type=\"checkbox\" tabindex=\"-1\" style=\"{1}\"/>")
        SafeHtml inputUnchecked(String id, SafeStyles style);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;

    private SafeHtml label;
    private SafeStyles additionalStyles;

    private SafeHtml tooltipFallback;

    public CheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
        super(dependsOnSelection, handlesSelection);
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.addAll(ElementTooltipUtils.HANDLED_CELL_EVENTS);
        return set;
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
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder, java.lang.String)
     */
    // TODO-GWT: copied from GWT CheckboxCell, with ID injected. Keep in sync on GWT upgrade.
    public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
        // Get the view data.
        Object key = context.getKey();
        Boolean viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        if (value != null && ((viewData != null) ? viewData : value)) {
            sb.append(templates.inputChecked(id, additionalStyles != null ? additionalStyles : SafeStylesUtils.fromTrustedString("")));
        } else {
            sb.append(templates.inputUnchecked(id, additionalStyles != null ? additionalStyles : SafeStylesUtils.fromTrustedString("")));
        }

        if (getLabel() != null && StringHelper.isNotNullOrEmpty(getLabel().asString())) {
            sb.append(getLabel());
        }
    }

    public Set<String> getParentConsumedEvents() {
        return super.getConsumedEvents();
    }

    public SafeHtml getLabel() {
        return label;
    }

    public void setLabel(SafeHtml newLabel) {
        label = newLabel;
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

    public void setAdditionalStyles(SafeStyles styles) {
        additionalStyles = styles;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Boolean value,
            NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
        ElementTooltipUtils.handleCellEvent(event, parent, getTooltip(value));
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

    public SafeHtml getTooltip(Boolean value) {
        return tooltipFallback;
    }

    @Override
    public void setTooltipFallback(SafeHtml tooltipFallback) {
        this.tooltipFallback = tooltipFallback;
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return true;
    }

}
