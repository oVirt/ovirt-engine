package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A Cell used to render SafeHtml. Supports tooltips.
 */
public class SafeHtmlCell extends AbstractCell<SafeHtml>  {

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            sb.appendHtmlConstant("<div id=\"" + id + "\" style='display:block'>"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(value);
            sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
        }
    }

}
