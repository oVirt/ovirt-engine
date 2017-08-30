package org.ovirt.engine.ui.webadmin.widget.table.cell;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class NullableButtonCell extends ButtonCell {

    @Override
    public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        if (data != null) {
            sb.appendHtmlConstant("<button type=\"button\" class=\"btn btn-default\" tabindex=\"-1\">"); //$NON-NLS-1$
            sb.append(data);
            sb.appendHtmlConstant("</button>"); //$NON-NLS-1$
        }
    }
}
