package org.ovirt.engine.ui.webadmin.widget.table.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.AbstractInputCell;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
/**
 * A cell used to render a drop-down list.
 */
public class CustomSelectionCell extends AbstractInputCell<String, String> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{0}</option>")
        SafeHtml deselected(String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
        SafeHtml selected(String option);

        @Template("<select id=\"{0}\" class=\"{1}\" tabindex=\"-1\" >")
        SafeHtml selectEnabled(String id, String classNames);

        @Template("<select id=\"{0}\" class=\"{1}\" tabindex=\"-1\" disabled>")
        SafeHtml selectDisabled(String id, String classNames);
    }

    private static CellTemplate template = GWT.create(CellTemplate.class);

    private final Map<String, Integer> indexForOption = new HashMap<>();

    private List<String> options;

    private boolean isEnabled = true;

    private String style;

    /**
     * Construct a new {@link com.google.gwt.cell.client.SelectionCell} with the specified options.
     *
     * @param options
     *            the options in the cell
     */
    public CustomSelectionCell(List<String> options) {
        super();
        this.options = new ArrayList<>(options);
        int index = 0;
        for (String option : options) {
            indexForOption.put(option, index++);
        }
    }
    /**
     * Events to sink. By default, we only sink mouse events that tooltips need. Override this
     * (and include addAll(super.getConsumedEvents())'s events!) if your cell needs to respond
     * to additional events.
     */
    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CHANGE);
        return set;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value,
            NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        String type = event.getType();
        if (BrowserEvents.CHANGE.equals(type)) {
            Object key = context.getKey();
            SelectElement select = parent.getFirstChild().cast();
            String newValue = options.get(select.getSelectedIndex());
            setViewData(key, newValue);
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        // Get the view data.
        Object key = context.getKey();
        String viewData = getViewData(key);
        if (viewData != null) {
            clearViewData(key);
            viewData = null;
        }

        if (isEnabled) {
            sb.append(template.selectEnabled(id, style));
        } else {
            sb.append(template.selectDisabled(id, style));
        }

        int index = 0;
        int selectedIndex = getSelectedIndex(value);
        for (String option : options) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(option));
            } else {
                sb.append(template.deselected(option));
            }
        }
        sb.appendHtmlConstant("</select>"); //$NON-NLS-1$
    }

    private int getSelectedIndex(String value) {
        return indexForOption.getOrDefault(value, -1);
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
        int index = 0;
        for (String option : options) {
            indexForOption.put(option, index++);
        }
    }

}
