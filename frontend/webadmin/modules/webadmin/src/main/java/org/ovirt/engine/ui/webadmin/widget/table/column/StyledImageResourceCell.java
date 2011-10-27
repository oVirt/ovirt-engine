package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class StyledImageResourceCell extends ImageResourceCell {

    @Override
    public void render(Context context, ImageResource value, SafeHtmlBuilder sb) {
        if (value != null) {
            SafeHtml html = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(value).getHTML());

            sb.appendHtmlConstant("<div style=\"" + "text-align: center; padding-top: 6px;" + "\">");
            sb.append(html);
            sb.appendHtmlConstant("</div>");
        }
    }

}
