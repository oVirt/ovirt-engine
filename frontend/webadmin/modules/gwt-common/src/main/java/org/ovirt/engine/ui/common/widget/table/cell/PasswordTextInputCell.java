package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class PasswordTextInputCell extends TextInputCell {

    interface PasswordTemplate extends SafeHtmlTemplates {
        @Template("<input type=\"password\" value=\"{0}\" tabindex=\"-1\"></input>")
        SafeHtml input(String value);
    }

    private static PasswordTemplate template;

    public PasswordTextInputCell() {
        if (template == null) {
            template = GWT.create(PasswordTemplate.class);
        }
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        ViewData viewData = getViewData(key);
        if (viewData != null && viewData.getCurrentValue().equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        String s = (viewData != null) ? viewData.getCurrentValue() : value;
        if (s != null) {
            sb.append(template.input(s));
        } else {
            sb.appendHtmlConstant("<input type=\"password\" tabindex=\"-1\"></input>"); //$NON-NLS-1$
        }
    }
}
