package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * A cell used to render a radio button. The value of the radio may be toggled using the ENTER key as well as via mouse
 * click.
 */
public class RadioboxCell extends AbstractEditableCell<Boolean, Boolean> implements EventHandlingCell, Cell<Boolean> {

    interface RadioboxCellTemplates extends SafeHtmlTemplates {
        @Template("<input id=\"{0}\" type=\"radio\" tabindex=\"-1\" checked/>")
        SafeHtml radioChecked(String id);
        @Template("<input id=\"{0}\" type=\"radio\" tabindex=\"-1\"/>")
        SafeHtml radioUnchecked(String id);
    }

    private static RadioboxCellTemplates templates = GWT.create(RadioboxCellTemplates.class);

    private final boolean dependsOnSelection;
    private final boolean handlesSelection;

    private SafeHtml tooltipFallback;

    /**
     * Construct a new {@link RadioboxCell}.
     */
    public RadioboxCell() {
        this(false, false);
    }

    /**
     * Construct a new {@link RadioboxCell} that optionally controls selection.
     *
     * @param dependsOnSelection
     *            true if the cell depends on the selection state
     * @param handlesSelection
     *            true if the cell modifies the selection state
     */
    public RadioboxCell(boolean dependsOnSelection, boolean handlesSelection) {
        this.dependsOnSelection = dependsOnSelection;
        this.handlesSelection = handlesSelection;
    }

    @Override
    public boolean dependsOnSelection() {
        return dependsOnSelection;
    }

    @Override
    public boolean handlesSelection() {
        return handlesSelection;
    }

    @Override
    public boolean isEditing(Context context, Element parent, Boolean value) {
        // A checkbox is never in "edit mode". There is no intermediate state
        // between checked and unchecked.
        return false;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Boolean value,
            NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
        String type = event.getType();

        boolean enterPressed = BrowserEvents.KEYDOWN.equals(type)
                && event.getKeyCode() == KeyCodes.KEY_ENTER;

        if (BrowserEvents.CHANGE.equals(type) || enterPressed) {
            InputElement input = parent.getFirstChild().cast();
            boolean isChecked = input.isChecked();

            /*
             * Toggle the value if the enter key was pressed and the cell handles selection or doesn't depend on
             * selection. If the cell depends on selection but doesn't handle selection, then ignore the enter key and
             * let the SelectionEventManager determine which keys will trigger a change.
             */
            if (enterPressed && (handlesSelection() || !dependsOnSelection())) {
                isChecked = !isChecked;
                input.setChecked(isChecked);
            }

            /*
             * Save the new value. However, if the cell depends on the selection, then do not save the value because we
             * can get into an inconsistent state.
             */

            if (value.booleanValue() != isChecked && !dependsOnSelection()) {
                setViewData(context.getKey(), isChecked);
            } else {
                clearViewData(context.getKey());
            }

            if (valueUpdater != null) {
                valueUpdater.update(isChecked);
            }
        }
        ElementTooltipUtils.handleCellEvent(event, parent, getTooltip(value));
    }

    @Override
    public void render(Context context, Boolean value, SafeHtmlBuilder sb, String id) {
        // Get the view data.
        Object key = context.getKey();
        Boolean viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        if (value != null && ((viewData != null) ? viewData : value)) {
            sb.append(templates.radioChecked(id));
        } else {
            sb.append(templates.radioUnchecked(id));
        }
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
        return EventHandlingCellMixin.inputHandlesClick(event)
                || ElementTooltipUtils.HANDLED_CELL_EVENTS.contains(event.getNativeEvent().getType());
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> consumedEvents = new HashSet<>(ElementTooltipUtils.HANDLED_CELL_EVENTS);
        consumedEvents.add(BrowserEvents.CHANGE);
        consumedEvents.add(BrowserEvents.KEYDOWN);
        return consumedEvents;
    }
}
