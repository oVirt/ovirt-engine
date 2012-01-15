package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A {@link Cell} used to render a drop-down list.
 */
public class CustomSelectionCell extends AbstractInputCell<String, String> {

    interface Template extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{0}</option>")
        SafeHtml deselected(String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
        SafeHtml selected(String option);
    }

    private static Template template;

    private final HashMap<String, Integer> indexForOption = new HashMap<String, Integer>();

    private final List<String> options;

    private boolean isEnabled = true;

    private String tooltip;

    /**
     * Construct a new {@link com.google.gwt.cell.client.SelectionCell} with the specified options.
     *
     * @param options
     *            the options in the cell
     */
    public CustomSelectionCell(List<String> options) {
        super("change");
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.options = new ArrayList<String>(options);
        int index = 0;
        for (String option : options) {
            indexForOption.put(option, index++);
        }
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value,
            NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if ("change".equals(type)) {
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
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        String viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        int selectedIndex = getSelectedIndex(viewData == null ? value : viewData);
        if (isEnabled) {
            sb.appendHtmlConstant("<select tabindex=\"-1\">");
        } else {
            sb.appendHtmlConstant("<select tabindex=\"-1\" title=\"" + tooltip + "\" disabled>");
        }
        int index = 0;
        for (String option : options) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(option));
            } else {
                sb.append(template.deselected(option));
            }
        }
        sb.appendHtmlConstant("</select>");
    }

    private int getSelectedIndex(String value) {
        Integer index = indexForOption.get(value);
        if (index == null) {
            return -1;
        }
        return index.intValue();
    }

    public void setEnabledWithToolTip(boolean isEnabled, String tooltip) {
        this.isEnabled = isEnabled;
        this.tooltip = tooltip;
    }
}
