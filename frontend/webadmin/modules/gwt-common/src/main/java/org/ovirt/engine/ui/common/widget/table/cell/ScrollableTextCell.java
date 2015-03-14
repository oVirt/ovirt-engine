package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * ScrollableTextCell. Does not support tooltips.
 *
 */
public class ScrollableTextCell extends TextInputCell {

    public interface CellTemplate extends SafeHtmlTemplates {
        @Template("<input id=\"{2}\" style=\"background: transparent; border: 0px; width: 95%; {1}\"" +
                "readonly=\"readonly\" type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
        SafeHtml input(String value, String customStyle, String id);
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        CellTemplate template = GWT.create(CellTemplate.class);
        sb.append(template.input(value, "", id)); //$NON-NLS-1$
    }

}
