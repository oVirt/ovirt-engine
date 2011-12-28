package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ScrollableTextCell extends TextInputCell {
    public interface Template extends SafeHtmlTemplates {
        @Template("<input style=\"background: transparent; border: 0px; width: 95%; {1}\"" +
                "readonly=\"readonly\" type=\"text\" value=\"{0}\" title=\"{0}\"  tabindex=\"-1\"></input>")
        SafeHtml input(String value, String customStyle);
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        Template template = GWT.create(Template.class);
        sb.append(template.input(value, ""));
    }
}
