package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * PasswordTextInputCell. Does not support tooltips.
 *
 */
public class PasswordTextInputCell extends TextInputCell {

    interface PasswordTemplate extends SafeHtmlTemplates {
        @Template("<input id=\"{0}\" type=\"password\" value=\"{1}\" tabindex=\"-1\"></input>")
        SafeHtml inputWithValue(String id, String value);

        @Template("<input id=\"{0}\" type=\"password\" tabindex=\"-1\"></input>")
        SafeHtml input(String id);
    }

    private static PasswordTemplate template;

    public PasswordTextInputCell() {
        if (template == null) {
            template = GWT.create(PasswordTemplate.class);
        }
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        // Get the view data.
        Object key = context.getKey();
        ViewData viewData = getViewData(key);
        if (viewData != null && viewData.getCurrentValue().equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        String s = (viewData != null) ? viewData.getCurrentValue() : value;
        if (s != null) {
            sb.append(template.inputWithValue(id, s));
        } else {
            sb.append(template.input(id));
        }
    }

}
