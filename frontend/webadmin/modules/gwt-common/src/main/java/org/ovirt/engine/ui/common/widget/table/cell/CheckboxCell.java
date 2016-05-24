package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * <p>
 * Base class for all Cells that would otherwise extend GWT CheckboxCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 * </p>
 */
public class CheckboxCell extends com.google.gwt.cell.client.CheckboxCell implements Cell<Boolean>, EventHandlingCell {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<input id=\"{0}\" type=\"checkbox\" tabindex=\"-1\" checked style=\"{1}\"/>")
        SafeHtml inputChecked(String id, String style);

        @Template("<input id=\"{0}\" type=\"checkbox\" tabindex=\"-1\" style=\"{1}\"/>")
        SafeHtml inputUnchecked(String id, String style);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;
    private SafeHtml label;
    private SafeHtml tooltip = SafeHtmlUtils.EMPTY_SAFE_HTML;
    private String additionalStyles;

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
            sb.append(templates.inputChecked(id, getAdditionalStyles()));
        } else {
            sb.append(templates.inputUnchecked(id, getAdditionalStyles()));
        }
        if (getLabel() != null && !StringUtils.isEmpty(getLabel().asString())) {
            sb.append(getLabel());
        }
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
        set.addAll(super.getConsumedEvents());
        return set;
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

    public String getAdditionalStyles() {
        String result = additionalStyles;
        if (result == null) {
            result = ""; //$NON-NLS-1$
        }
        return result;
    }

    public void setAdditionalStyles(String styles) {
        additionalStyles = styles;
    }

    /**
     * Handle events for this cell.
     *
     * @see org.ovirt.engine.ui.common.widget.table.cell.Cell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, com.google.gwt.dom.client.Element, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtml, com.google.gwt.dom.client.NativeEvent, com.google.gwt.cell.client.ValueUpdater)
     */
    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent,
            Boolean value, SafeHtml tooltipContent, NativeEvent event, ValueUpdater<Boolean> valueUpdater) {

        // if the Column did not provide a tooltip, give the Cell a chance to render one using the cell value C
        if (tooltipContent == null) {
            tooltipContent = getTooltip(value);
        }
        TooltipMixin.handleTooltipEvent(parent, tooltipContent, event);

        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

    public SafeHtml getTooltip(Boolean value) {
        return tooltip;
    }

    public void setTooltip(SafeHtml tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return true;
    }
}
