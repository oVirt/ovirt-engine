package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A Cell used to render SafeHtml. Supports tooltips.
 */
public class SafeHtmlCell extends AbstractCell<SafeHtml>  {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div id=\"{0}\" style='display:block'>{1}</div>")
        SafeHtml div(String id, SafeHtml html);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            sb.append(templates.div(id, value));
        }
    }

}
