package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class StyledImageResourceCell extends ImageResourceCell {

    private String style = "line-height: 100%; text-align: center; vertical-align: middle;"; //$NON-NLS-1$

    @Override
    public void render(Context context, ImageResource value, SafeHtmlBuilder sb) {
        if (value != null) {
            SafeHtml html = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(value).getHTML());

            sb.appendHtmlConstant("<div style=\"" + style + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(html);
            sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
        }
    }

    public void setStyle(String style) {
        this.style = style;
    }

}
